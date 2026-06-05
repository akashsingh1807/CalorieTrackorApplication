'use client';
import { useState, useRef, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { aiAPI, mealAPI } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import {
  Type, Sparkles, Plus, Trash2,
  CheckCircle2, AlertCircle, ChevronDown, X, Loader2,
  Mic, Calendar
} from 'lucide-react';


type FoodItem = {
  name: string; calories: number; protein: number; carbs: number; fat: number; servingSize: string;
};

const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'];

// Rough nutrition estimates for demo purposes
function estimateMacros(name: string): Omit<FoodItem, 'name'> {
  const n = name.toLowerCase();
  if (n.includes('roti') || n.includes('chapati')) return { calories: 120, protein: 3.5, carbs: 22, fat: 2.5, servingSize: '1 piece' };
  if (n.includes('dal')) return { calories: 180, protein: 10, carbs: 25, fat: 4, servingSize: '1 bowl' };
  if (n.includes('rice')) return { calories: 200, protein: 4, carbs: 44, fat: 0.5, servingSize: '1 cup cooked' };
  if (n.includes('chicken')) return { calories: 240, protein: 30, carbs: 0, fat: 12, servingSize: '100g' };
  if (n.includes('egg')) return { calories: 70, protein: 6, carbs: 0.5, fat: 5, servingSize: '1 large' };
  if (n.includes('salad')) return { calories: 80, protein: 3, carbs: 10, fat: 3, servingSize: '1 bowl' };
  if (n.includes('banana')) return { calories: 90, protein: 1, carbs: 23, fat: 0.3, servingSize: '1 medium' };
  if (n.includes('milk')) return { calories: 120, protein: 6, carbs: 12, fat: 4, servingSize: '240ml' };
  if (n.includes('paneer')) return { calories: 265, protein: 18, carbs: 1.2, fat: 21, servingSize: '100g' };
  if (n.includes('samosa')) return { calories: 260, protein: 5, carbs: 32, fat: 13, servingSize: '1 piece' };
  return { calories: 150, protein: 5, carbs: 20, fat: 5, servingSize: '1 serving' };
}

export default function LogMealPage() {
  const router = useRouter();
  const { user } = useAuth();

  const [mealType, setMealType] = useState('LUNCH');
  const [textInput, setTextInput] = useState('');
  const [aiLoading, setAiLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [foodItems, setFoodItems] = useState<FoodItem[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  // Retroactive Date State & Voice Input States
  const [mealDate, setMealDate] = useState(() => {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  });

  const [isRecording, setIsRecording] = useState(false);
  const recognitionRef = useRef<any>(null);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      const dateParam = params.get('date');
      if (dateParam && /^\d{4}-\d{2}-\d{2}$/.test(dateParam)) {
        setMealDate(dateParam);
      }
    }
  }, []);

  const startSpeechToText = () => {
    if (typeof window === 'undefined') return;
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setError('Speech recognition is not supported in this browser. Please try Chrome or Safari.');
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = false;
      recognition.lang = 'en-US';

      recognition.onstart = () => {
        setIsRecording(true);
      };

      recognition.onresult = (event: any) => {
        let transcript = '';
        for (let i = event.resultIndex; i < event.results.length; i++) {
          if (event.results[i].isFinal) {
            transcript += event.results[i][0].transcript;
          }
        }
        if (transcript) {
          setTextInput(prev => prev ? `${prev} ${transcript.trim()}` : transcript.trim());
        }
      };

      recognition.onerror = (event: any) => {
        console.error('Speech recognition error:', event.error);
        if (event.error !== 'no-speech') {
          setError(`Speech recognition error: ${event.error}`);
        }
        setIsRecording(false);
      };

      recognition.onend = () => {
        setIsRecording(false);
      };

      recognitionRef.current = recognition;
      recognition.start();
    } catch (e: any) {
      console.error(e);
      setError('Failed to start speech recognition.');
      setIsRecording(false);
    }
  };

  const stopSpeechToText = () => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
      setIsRecording(false);
    }
  };

  const toggleSpeechToText = () => {
    if (isRecording) {
      stopSpeechToText();
    } else {
      startSpeechToText();
    }
  };

  const runAI = async () => {
    setAiLoading(true);
    setError('');
    try {
      let items: FoodItem[] = [];
      if (textInput.trim()) {
        const res = await aiAPI.analyzeText(textInput);
        items = res.data.foodItems || [];
      }
      setFoodItems(items);
    } catch {
      setError('AI analysis failed. Please try again.');
    } finally {
      setAiLoading(false);
    }
  };

  const updateItem = (idx: number, key: keyof FoodItem, val: string | number) => {
    setFoodItems(prev => prev.map((it, i) => i === idx ? { ...it, [key]: val } : it));
  };

  const removeItem = (idx: number) => setFoodItems(prev => prev.filter((_, i) => i !== idx));

  const addManualItem = () => {
    setFoodItems(prev => [...prev, { name: '', calories: 0, protein: 0, carbs: 0, fat: 0, servingSize: '1 serving' }]);
  };

  const handleSave = async () => {
    if (foodItems.length === 0) { setError('Please add at least one food item.'); return; }
    setSaving(true);
    setError('');
    try {
      // Build retroactive timestamp: blend selected mealDate with current client time
      const currentDate = new Date();
      const timeString = currentDate.toTimeString().split(' ')[0]; // HH:MM:SS
      const timestamp = mealDate ? `${mealDate}T${timeString}` : undefined;

      await mealAPI.logMeal({
        mealType,
        foodItems: foodItems.map(f => ({
          name: f.name, calories: Number(f.calories), protein: Number(f.protein),
          carbs: Number(f.carbs), fat: Number(f.fat), servingSize: f.servingSize,
        })),
        timestamp,
      });
      setSuccess(true);
      setTimeout(() => router.push('/dashboard'), 1500);
    } catch {
      setError('Failed to save meal. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (success) {
    return (
      <div className="page-wrapper" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 'calc(100vh - 64px)' }}>
        <div className="animate-scale-in" style={{ textAlign: 'center' }}>
          <CheckCircle2 size={64} color="var(--accent-green)" style={{ marginBottom: '1rem' }} />
          <h2 style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>Meal logged! 🎉</h2>
          <p style={{ color: 'var(--text-secondary)' }}>Redirecting to dashboard…</p>
        </div>
      </div>
    );
  }

  const totalCals = foodItems.reduce((s, f) => s + Number(f.calories), 0);

  return (
    <div className="page-wrapper">
      <style dangerouslySetInnerHTML={{ __html: `
        @keyframes micPulse {
          0% {
            transform: scale(1);
            box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7);
          }
          70% {
            transform: scale(1.1);
            box-shadow: 0 0 0 10px rgba(239, 68, 68, 0);
          }
          100% {
            transform: scale(1);
            box-shadow: 0 0 0 0 rgba(239, 68, 68, 0);
          }
        }
        .mic-pulse-active {
          animation: micPulse 1.5s infinite ease-in-out !important;
          background: var(--accent-red, #ef4444) !important;
          color: white !important;
          border: none !important;
        }
      `}} />
      <div className="dashboard-main" style={{ maxWidth: 760, margin: '0 auto' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.6rem', marginBottom: '0.25rem' }}>Log a Meal</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Use AI to identify food or enter it manually</p>
        </div>

        {error && <div className="alert alert-error" style={{ marginBottom: '1rem' }}><AlertCircle size={16} />{error}</div>}

        {/* Meal Type & Date Selectors */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem', marginBottom: '1.25rem' }}>
          <div className="raw-card" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
            <label className="input-label" style={{ marginBottom: '0.6rem', display: 'block' }}>Meal Type</label>
            <div style={{ display: 'flex', gap: '0.6rem', flexWrap: 'wrap' }}>
              {MEAL_TYPES.map(t => (
                <button key={t} onClick={() => setMealType(t)} className={`btn ${mealType === t ? 'btn-primary' : 'btn-ghost'} btn-sm`}>
                  {t === 'BREAKFAST' ? '☀️' : t === 'LUNCH' ? '🌞' : t === 'DINNER' ? '🌙' : '🍎'} {t.charAt(0) + t.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
          </div>

          <div className="raw-card" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
            <label className="input-label" style={{ marginBottom: '0.6rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Calendar size={14} /> Meal Date
            </label>
            <input
              type="date"
              className="input"
              value={mealDate}
              onChange={(e) => setMealDate(e.target.value)}
              style={{ width: '100%', padding: '0.4rem 0.75rem' }}
            />
          </div>
        </div>

        {/* Input Area */}
        <div className="raw-card" style={{ padding: '1.25rem', marginBottom: '1.25rem' }}>
          <div style={{ position: 'relative' }}>
            <textarea
              className="input"
              placeholder="Describe what you ate… e.g. '2 rotis with dal makhani and a glass of lassi'"
              value={textInput}
              onChange={(e) => setTextInput(e.target.value)}
              rows={4}
              style={{ resize: 'vertical', paddingRight: '3.5rem' }}
            />
            <button
              type="button"
              onClick={toggleSpeechToText}
              className={`btn btn-icon ${isRecording ? 'mic-pulse-active' : ''}`}
              style={{
                position: 'absolute',
                right: '0.75rem',
                bottom: '0.75rem',
                borderRadius: '50%',
                width: '2.2rem',
                height: '2.2rem',
                padding: 0,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: isRecording ? 'var(--accent-red, #ef4444)' : 'rgba(255, 255, 255, 0.08)',
                border: isRecording ? 'none' : '1px solid var(--border-subtle)',
                color: isRecording ? '#fff' : 'var(--text-secondary)',
                cursor: 'pointer',
                transition: 'all 0.3s ease',
              }}
              title={isRecording ? 'Stop recording' : 'Start voice input'}
            >
              <Mic size={16} />
            </button>
          </div>

          <button
            onClick={runAI}
            className="btn btn-primary"
            disabled={aiLoading || !textInput.trim()}
            style={{ marginTop: '1rem', width: '100%' }}
          >
            {aiLoading ? <span className="spinner" /> : <><Sparkles size={16} /> Identify with AI</>}
          </button>
        </div>

        {/* Food Items Editor */}
        {foodItems.length > 0 && (
          <div className="raw-card" style={{ padding: '1.5rem', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1rem' }}>Food Items</h3>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Total: <strong style={{ color: 'var(--accent-orange)' }}>{Math.round(totalCals)} kcal</strong></span>
                <button onClick={addManualItem} className="btn btn-ghost btn-sm"><Plus size={14} /> Add</button>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {foodItems.map((item, idx) => (
                <div key={idx} style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)', padding: '1rem' }}>
                  <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '0.75rem', alignItems: 'center' }}>
                    <input
                      className="input"
                      placeholder="Food name"
                      value={item.name}
                      onChange={(e) => updateItem(idx, 'name', e.target.value)}
                      style={{ flex: 2 }}
                    />
                    <input
                      className="input"
                      placeholder="Serving"
                      value={item.servingSize}
                      onChange={(e) => updateItem(idx, 'servingSize', e.target.value)}
                      style={{ flex: 1 }}
                    />
                    <button onClick={() => removeItem(idx)} className="btn btn-danger btn-sm btn-icon">
                      <Trash2 size={14} />
                    </button>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.5rem' }}>
                    {(['calories', 'protein', 'carbs', 'fat'] as const).map(key => (
                      <div key={key} className="input-group">
                        <label className="input-label">{key.charAt(0).toUpperCase() + key.slice(1)}{key === 'calories' ? ' (kcal)' : ' (g)'}</label>
                        <input
                          type="number"
                          className="input"
                          value={item[key]}
                          onChange={(e) => updateItem(idx, key, parseFloat(e.target.value) || 0)}
                          min={0}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Manual Add (no AI) */}
        {foodItems.length === 0 && (
          <div className="raw-card" style={{ padding: '1.25rem', marginBottom: '1.25rem', textAlign: 'center' }}>
            <p style={{ color: 'var(--text-muted)', marginBottom: '0.75rem', fontSize: '0.875rem' }}>Or add food items manually</p>
            <button onClick={addManualItem} className="btn btn-secondary"><Plus size={15} /> Add Food Item</button>
          </div>
        )}

        {/* Save Button */}
        <button
          onClick={handleSave}
          className="btn btn-primary btn-lg"
          disabled={saving || foodItems.length === 0}
          style={{ width: '100%' }}
        >
          {saving ? <span className="spinner" /> : <>Save Meal · {Math.round(totalCals)} kcal</>}
        </button>

        <details style={{ marginTop: '1rem' }}>
          <summary style={{ cursor: 'pointer', color: 'var(--text-muted)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <ChevronDown size={14} /> Meal type: {mealType}
          </summary>
        </details>
      </div>
    </div>
  );
}
