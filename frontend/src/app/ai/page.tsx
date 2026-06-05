'use client';
import { useState } from 'react';
import { aiAPI } from '@/lib/api';
import { Zap, Send, Loader2, Apple, Info, Sparkles } from 'lucide-react';

export default function AIPage() {
  const [text, setText] = useState('');
  const [analyzing, setAnalyzing] = useState(false);
  const [result, setResult] = useState<any>(null);

  const handleTextAnalyze = async () => {
    if (!text) return;
    setAnalyzing(true);
    try {
      const res = await aiAPI.analyzeText(text);
      setResult(res.data);
    } catch (err) {
      console.error('AI Analysis failed:', err);
    } finally {
      setAnalyzing(false);
    }
  };

  return (
    <div className="page-wrapper">
      <div className="dashboard-main">
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.6rem', marginBottom: '0.25rem' }}>AI Insights</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Powered by Gemini for precision nutrition</p>
        </div>

        <div className="dashboard-grid grid-cols-2">
          {/* Input Section */}
          <div style={{ display: 'grid', gap: '1.5rem' }}>
            <div className="raw-card" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.25rem' }}>
                <Zap size={18} color="var(--accent-blue)" />
                <h3 style={{ fontSize: '1rem' }}>Smart Text Analysis</h3>
              </div>
              <textarea
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Example: I had 2 scrambled eggs with toast and a coffee"
                className="input"
                style={{ width: '100%', minHeight: '100px', marginBottom: '1rem', resize: 'none' }}
              />
              <button 
                onClick={handleTextAnalyze} 
                disabled={analyzing || !text}
                className="btn btn-primary w-full"
              >
                {analyzing ? <Loader2 className="animate-spin" size={18} /> : <Sparkles size={18} />}
                Analyze Text
              </button>
            </div>
          </div>

          {/* Results Section */}
          <div className="raw-card" style={{ padding: '2rem', display: 'flex', flexDirection: 'column' }}>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Info size={18} /> Analysis Results
            </h3>
            
            {result ? (
              <div style={{ animation: 'fadeIn 0.3s ease', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--accent-green)' }}>
                  Estimated Calories: {result.totalCalories || result.calories || 0} kcal
                </div>
                
                <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                    <Apple size={14} /> {result.foodName || result.name || 'Analyzed Item'}
                  </div>
                  <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                    P: {result.protein}g • C: {result.carbohydrates || result.carbs}g • F: {result.fat || result.fats}g
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                    Serving: {result.servingSize} {result.servingUnit}
                  </div>
                </div>

                <div style={{ marginTop: '1rem' }}>
                  <h4 style={{ fontSize: '0.9rem', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>AI Reasoning & Raw Data:</h4>
                  <textarea
                    readOnly
                    value={JSON.stringify(result, null, 2)}
                    className="input"
                    style={{ 
                      width: '100%', 
                      minHeight: '200px', 
                      fontFamily: 'monospace', 
                      fontSize: '0.8rem',
                      resize: 'vertical',
                      backgroundColor: 'rgba(0,0,0,0.2)'
                    }}
                  />
                </div>

                <div style={{ marginTop: '1rem', padding: '1rem', border: '1px solid rgba(77,159,255,0.2)', borderRadius: 'var(--radius-md)', background: 'rgba(77,159,255,0.05)' }}>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    <strong>Note:</strong> These are AI-generated estimates. Actual nutritional values may vary based on ingredients and preparation.
                  </p>
                </div>
              </div>
            ) : analyzing ? (
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
                <Loader2 className="animate-spin" size={40} style={{ marginBottom: '1rem' }} />
                <p>Gemini is thinking...</p>
              </div>
            ) : (
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', textAlign: 'center' }}>
                <Sparkles size={40} style={{ marginBottom: '1rem', opacity: 0.2 }} />
                <p>Analyze text to see nutritional insights</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
