import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../api/client';

const CHAT_WS_URL = import.meta.env.VITE_CHAT_WS_URL || 'http://localhost:8082/ws-connect';

export default function Chat() {
  const { user, token, logout } = useAuth();
  const myUserId = Number(user.sub);

  const [connected, setConnected] = useState(false);
  const [receiverId, setReceiverId] = useState('');
  const [content, setContent] = useState('');
  const [messages, setMessages] = useState([]);
  const [activeConversationWith, setActiveConversationWith] = useState(null);

  const clientRef = useRef(null);
  const bottomRef = useRef(null);
  const openConversationIdRef = useRef(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${CHAT_WS_URL}?userId=${myUserId}`),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/user/queue/messages', (frame) => {
          const incoming = JSON.parse(frame.body);
          if (incoming.senderId === openConversationIdRef.current) {
            setMessages((prev) => [...prev, incoming]);
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => console.error('STOMP hatası:', frame.headers['message']),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [myUserId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  function handleSend(event) {
    event.preventDefault();
    if (!content.trim() || !receiverId) return;

    const otherUserId = Number(receiverId);
    const payload = {
      senderId: myUserId,
      receiverId: otherUserId,
      content,
    };

    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(payload),
    });

    openConversationIdRef.current = otherUserId;
    setActiveConversationWith(otherUserId);

    setMessages((prev) => [
      ...prev,
      { ...payload, id: crypto.randomUUID(), timestamp: new Date().toISOString() },
    ]);
    setContent('');
  }

  async function handleOpenConversation() {
    if (!receiverId) return;
    const otherUserId = Number(receiverId);

    try {
      const history = await apiRequest(`/api/messages/history?withUserId=${otherUserId}`, {
        token,
      });
      openConversationIdRef.current = otherUserId;
      setActiveConversationWith(otherUserId);
      setMessages(history);
    } catch (err) {
      console.error('Sohbet geçmişi alınamadı:', err);
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-100">
      <header className="bg-white shadow px-4 py-3 flex justify-between items-center">
        <div>
          <span className="font-semibold">{user.username}</span>
          <span className={`ml-2 text-xs ${connected ? 'text-green-600' : 'text-red-600'}`}>
            {connected ? 'Bağlı' : 'Bağlanıyor...'}
          </span>
          {activeConversationWith && (
            <p className="text-xs text-gray-500">Sohbet: Kullanıcı {activeConversationWith}</p>
          )}
        </div>
        <button onClick={logout} className="text-sm text-gray-500 hover:text-gray-800">
          Çıkış yap
        </button>
      </header>

      <div className="flex-1 overflow-y-auto p-4 space-y-2">
        {messages.length === 0 && (
          <p className="text-center text-gray-400 text-sm mt-8">
            {activeConversationWith
              ? 'Henüz mesaj yok.'
              : 'Bir sohbet açmak için alıcı ID gir ve "Sohbeti Aç" butonuna bas.'}
          </p>
        )}
        {messages.map((m) => (
          <div
            key={m.id}
            className={`max-w-xs p-2 rounded-lg ${
              m.senderId === myUserId ? 'ml-auto bg-blue-600 text-white' : 'bg-white'
            }`}
          >
            <p className="text-sm">{m.content}</p>
            <p className="text-[10px] opacity-70">
              {m.senderId === myUserId ? 'Ben' : `Kullanıcı ${m.senderId}`} → {m.receiverId}
            </p>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      <form onSubmit={handleSend} className="bg-white border-t p-3 flex gap-2">
        <input
          value={receiverId}
          onChange={(e) => setReceiverId(e.target.value)}
          placeholder="Alıcı ID"
          className="w-24 border rounded px-2"
        />
        <button
          type="button"
          onClick={handleOpenConversation}
          disabled={!receiverId}
          className="bg-gray-200 px-3 rounded text-sm disabled:opacity-50"
        >
          Sohbeti Aç
        </button>
        <input
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Mesaj yaz..."
          className="flex-1 border rounded px-2"
        />
        <button
          type="submit"
          disabled={!connected}
          className="bg-blue-600 text-white px-4 rounded disabled:opacity-50"
        >
          Gönder
        </button>
      </form>
    </div>
  );
}
