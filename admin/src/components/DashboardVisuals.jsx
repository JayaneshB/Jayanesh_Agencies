import { ArrowUpRight } from 'lucide-react';

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max);
}

function formatAmount(value) {
  return `₹${Number(value || 0).toLocaleString()}`;
}

function createSeries(points, width, height, padding) {
  if (!points.length) return { line: '', area: '', dots: [] };

  const maxValue = Math.max(...points.map((point) => Number(point.value) || 0), 1);
  const usableWidth = width - padding * 2;
  const usableHeight = height - padding * 2;

  const coords = points.map((point, index) => {
    const x = points.length === 1 ? width / 2 : padding + (usableWidth * index) / (points.length - 1);
    const normalized = clamp((Number(point.value) || 0) / maxValue, 0, 1);
    const y = height - padding - normalized * usableHeight;
    return { x, y, value: point.value, label: point.label };
  });

  const line = coords.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ');
  const area = `${line} L ${coords[coords.length - 1].x} ${height - padding} L ${coords[0].x} ${height - padding} Z`;

  return { line, area, dots: coords };
}

export function MetricCard({ title, value, hint, icon: Icon, accent = 'emerald', change }) {
  const gradients = {
    emerald: 'from-emerald-700 to-emerald-500',
    teal: 'from-teal-700 to-teal-500',
    slate: 'from-slate-700 to-slate-500',
    amber: 'from-amber-600 to-amber-400',
    red: 'from-red-600 to-red-500',
  };

  return (
    <div className="app-kpi-card">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-medium text-slate-500">{title}</p>
          <p className="mt-2 text-3xl font-semibold tracking-tight text-slate-900">{value}</p>
          {hint ? <p className="mt-2 text-xs text-slate-500">{hint}</p> : null}
        </div>
        <div className={`grid h-11 w-11 place-items-center rounded-full bg-gradient-to-br ${gradients[accent] || gradients.emerald} text-white shadow-sm`}>
          {Icon ? <Icon size={18} /> : <ArrowUpRight size={18} />}
        </div>
      </div>

      {change ? (
        <div className="mt-4 inline-flex items-center gap-2 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
          <span className="grid h-4 w-4 place-items-center rounded-full bg-emerald-100 text-[10px]">↑</span>
          {change}
        </div>
      ) : null}
    </div>
  );
}

export function TrendChartCard({ title, subtitle, points, footerNote }) {
  const width = 760;
  const height = 280;
  const padding = 28;
  const { line, area, dots } = createSeries(points, width, height, padding);

  return (
    <div className="app-surface overflow-hidden">
      <div className="app-card-head">
        <div>
          <h3 className="app-card-title">{title}</h3>
          {subtitle ? <p className="text-xs text-slate-500">{subtitle}</p> : null}
        </div>
        <div className="app-chip">Revenue trend</div>
      </div>

      <div className="p-5">
        <svg viewBox={`0 0 ${width} ${height}`} className="h-[250px] w-full overflow-visible">
          <defs>
            <linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="rgba(16,185,129,0.22)" />
              <stop offset="100%" stopColor="rgba(16,185,129,0)" />
            </linearGradient>
            <linearGradient id="trendLine" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="#0f7a4f" />
              <stop offset="100%" stopColor="#14b87a" />
            </linearGradient>
          </defs>

          {[0.25, 0.5, 0.75].map((fraction) => (
            <line
              key={fraction}
              x1={padding}
              x2={width - padding}
              y1={padding + (height - padding * 2) * fraction}
              y2={padding + (height - padding * 2) * fraction}
              stroke="rgba(148,163,184,0.18)"
              strokeDasharray="6 6"
            />
          ))}

          {area ? <path d={area} fill="url(#trendFill)" /> : null}
          {line ? <path d={line} fill="none" stroke="url(#trendLine)" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" /> : null}

          {dots.map((dot) => (
            <g key={`${dot.label}-${dot.x}-${dot.y}`}>
              <circle cx={dot.x} cy={dot.y} r="6" fill="#ffffff" stroke="#0f7a4f" strokeWidth="4" />
            </g>
          ))}
        </svg>

        <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {points.map((point) => (
            <div key={point.label} className="rounded-2xl bg-slate-50 px-3 py-2">
              <p className="text-[11px] uppercase tracking-[0.16em] text-slate-400">{point.label}</p>
              <p className="mt-1 text-sm font-semibold text-slate-900">{formatAmount(point.value)}</p>
            </div>
          ))}
        </div>

        {footerNote ? <p className="mt-4 text-xs text-slate-500">{footerNote}</p> : null}
      </div>
    </div>
  );
}

export function DonutCard({ title, subtitle, items, centerValue, centerLabel }) {
  const radius = 78;
  const stroke = 18;
  const circumference = 2 * Math.PI * radius;
  const total = items.reduce((sum, item) => sum + (Number(item.value) || 0), 0) || 1;
  let offset = 0;

  return (
    <div className="app-surface overflow-hidden">
      <div className="app-card-head">
        <div>
          <h3 className="app-card-title">{title}</h3>
          {subtitle ? <p className="text-xs text-slate-500">{subtitle}</p> : null}
        </div>
        <div className="app-chip">Status mix</div>
      </div>

      <div className="grid gap-5 p-5 md:grid-cols-[1.1fr_0.9fr]">
        <div className="relative mx-auto flex aspect-square w-full max-w-[260px] items-center justify-center">
          <svg viewBox="0 0 220 220" className="h-full w-full -rotate-90">
            <circle cx="110" cy="110" r={radius} fill="none" stroke="rgba(15,23,42,0.08)" strokeWidth={stroke} />
            {items.map((item, index) => {
              const value = Number(item.value) || 0;
              const length = (value / total) * circumference;
              const dashOffset = circumference - offset;
              offset += length;
              return (
                <circle
                  key={`${item.label}-${index}`}
                  cx="110"
                  cy="110"
                  r={radius}
                  fill="none"
                  stroke={item.color}
                  strokeWidth={stroke}
                  strokeLinecap="round"
                  strokeDasharray={`${length} ${circumference - length}`}
                  strokeDashoffset={dashOffset}
                />
              );
            })}
          </svg>
          <div className="absolute text-center">
            <p className="text-4xl font-semibold tracking-tight text-slate-900">{centerValue}</p>
            <p className="mt-1 text-xs uppercase tracking-[0.18em] text-slate-500">{centerLabel}</p>
          </div>
        </div>

        <div className="flex flex-col justify-center gap-3">
          {items.map((item) => {
            const value = Number(item.value) || 0;
            const percent = Math.round((value / total) * 100);
            return (
              <div key={item.label} className="rounded-2xl border border-slate-100 bg-slate-50 px-4 py-3">
                <div className="flex items-center justify-between gap-3 text-sm">
                  <div className="flex items-center gap-3">
                    <span className="h-3.5 w-3.5 rounded-full" style={{ backgroundColor: item.color }} />
                    <span className="font-medium text-slate-700">{item.label}</span>
                  </div>
                  <span className="font-semibold text-slate-900">{value}</span>
                </div>
                <div className="mt-2 h-2 overflow-hidden rounded-full bg-white">
                  <div className="h-full rounded-full" style={{ width: `${percent}%`, backgroundColor: item.color }} />
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export function BarListCard({ title, subtitle, items, emptyLabel = 'No items to show.' }) {
  const maxValue = Math.max(...items.map((item) => Number(item.value) || 0), 1);

  return (
    <div className="app-surface overflow-hidden">
      <div className="app-card-head">
        <div>
          <h3 className="app-card-title">{title}</h3>
          {subtitle ? <p className="text-xs text-slate-500">{subtitle}</p> : null}
        </div>
      </div>

      <div className="space-y-3 p-5">
        {items.length === 0 ? (
          <p className="rounded-2xl bg-slate-50 px-4 py-4 text-sm text-slate-500">{emptyLabel}</p>
        ) : (
          items.map((item) => {
            const value = Number(item.value) || 0;
            const percent = (value / maxValue) * 100;
            return (
              <div key={item.label} className="rounded-2xl border border-slate-100 bg-slate-50 p-4">
                <div className="flex items-center justify-between gap-4 text-sm">
                  <span className="font-medium text-slate-700">{item.label}</span>
                  <span className="font-semibold text-slate-900">{item.valueLabel || value}</span>
                </div>
                <div className="mt-3 h-2 overflow-hidden rounded-full bg-white">
                  <div className="h-full rounded-full bg-gradient-to-r from-emerald-700 to-teal-500" style={{ width: `${percent}%` }} />
                </div>
                {item.meta ? <p className="mt-2 text-xs text-slate-500">{item.meta}</p> : null}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
