from PIL import Image, ImageDraw, ImageFont
import os

def rr(draw, xy, r, fill):
    x1,y1,x2,y2 = xy
    draw.rectangle([x1+r,y1,x2-r,y2], fill=fill)
    draw.rectangle([x1,y1+r,x2,y2-r], fill=fill)
    draw.ellipse([x1,y1,x1+r*2,y1+r*2], fill=fill)
    draw.ellipse([x2-r*2,y1,x2,y1+r*2], fill=fill)
    draw.ellipse([x1,y2-r*2,x1+r*2,y2], fill=fill)
    draw.ellipse([x2-r*2,y2-r*2,x2,y2], fill=fill)

BG  = (13,13,26);   SURF = (26,26,44);   HIGH = (42,42,68)
ORG = (255,107,0);  TEX  = (255,255,255); DIM  = (136,136,153)
SUCC= (0,191,165);  HR   = (255,71,87);   W    = 360

FONT_PATH = "C:/Windows/Fonts/arial.ttf"
BOLD_PATH = "C:/Windows/Fonts/arialbd.ttf"
def fnt(size, bold=False):
    try: return ImageFont.truetype(BOLD_PATH if bold else FONT_PATH, size)
    except: return ImageFont.load_default()

F_SM = fnt(16); F_MD = fnt(20); F_LG = fnt(26, bold=True); F_XL = fnt(42, bold=True)

def t(d, x, y, text, color=TEX, font=None, anchor="mm"):
    try:    d.text((x, y), text, fill=color, font=font or F_MD, anchor=anchor)
    except: d.text((x, y), text, fill=color)

def new_img():
    img = Image.new("RGB", (W, W), BG)
    return img, ImageDraw.Draw(img)

BASE = "D:/GymBuddy/gym-tracker/zepp-watch-app/assets/screenshots"
os.makedirs(BASE, exist_ok=True)

# ── 1. HOME ─────────────────────────────────────────────────
img, d = new_img()
# Top bar
rr(d, [0,0,W,56], 0, SURF)
t(d, W//2, 18, "GymBuddy", ORG, F_LG)
t(d, W//2, 42, "Last: 25/03  \u2022  18 sets  \u2022  52m", DIM, F_SM)
t(d, W//2, 68, "\u2665  72 bpm", HR, F_SM)
# Start button
rr(d, [28,90,332,156], 33, ORG)
t(d, W//2, 123, "Start Workout", TEX, F_LG)
# History button
rr(d, [28,166,332,220], 27, SURF)
t(d, W//2, 193, "History", DIM, F_MD)
# Stats
for i, (val, label, c) in enumerate([("18","Sets",TEX), ("52m","Duration",TEX), ("1.4t","Volume",ORG)]):
    x = 12 + i*116
    rr(d, [x,234,x+108,310], 12, HIGH)
    t(d, x+54, 262, val, c, F_LG)
    t(d, x+54, 294, label, DIM, F_SM)
img.save(f"{BASE}/01_home.png")
print("1 done")

# ── 2. EXERCISE SELECT ────────────────────────────────────────
img, d = new_img()
rr(d, [0,0,W,54], 0, SURF)
t(d, W//2, 14, "Choose Exercise", TEX, fnt(24, bold=True))
cats = [("All", ORG, TEX), ("Chest", SURF, DIM), ("Back", SURF, DIM), ("Legs", SURF, DIM)]
cx = 8
for label, bg, fc in cats:
    cw = 82
    rr(d, [cx,58,cx+cw,82], 12, bg)
    t(d, cx+41, 70, label, fc, F_SM)
    cx += 86
exercises = [
    ("Bench Press", "Chest",     (255,107,53)),
    ("Squat",       "Legs",      (129,212,250)),
    ("Deadlift",    "Back",      (79,195,247)),
    ("OHP",         "Shoulders", (255,179,0)),
    ("Pull-Ups",    "Back",      (79,195,247)),
]
y = 92
for name, cat, dot in exercises:
    rr(d, [8,y,W-8,y+56], 10, SURF)
    d.ellipse([14,y+22,22,y+30], fill=dot)
    t(d, 30, y+12, name, TEX, F_MD, "lt")
    t(d, W-14, y+12, cat, DIM, F_SM, "rt")
    y += 60
img.save(f"{BASE}/02_exercise_select.png")
print("2 done")

# ── 3. LOG SET ────────────────────────────────────────────────
img, d = new_img()
rr(d, [0,0,W,54], 0, SURF)
t(d, W//2, 12, "Bench Press", TEX, fnt(24, bold=True))
t(d, W//2, 36, "Set 3", ORG, F_SM)
# Weight
rr(d, [8,60,82,122], 31, HIGH);  t(d, 45,  91, "\u2212", TEX, F_XL)
t(d, W//2, 91, "82.5 kg", TEX, F_XL)
rr(d, [278,60,352,122], 31, HIGH); t(d, 315, 91, "+", TEX, F_XL)
t(d, W//2, 132, "WEIGHT", DIM, F_SM)
# Reps
rr(d, [46,148,102,196], 24, HIGH); t(d, 74,  172, "\u2212", TEX, F_LG)
t(d, W//2, 172, "8", TEX, F_XL)
rr(d, [258,148,314,196], 24, HIGH); t(d, 286, 172, "+", TEX, F_LG)
t(d, W//2, 206, "REPS", DIM, F_SM)
t(d, W//2, 228, "Sets: 80\u00d78  \u2022  80\u00d78", DIM, F_SM)
t(d, W//2, 248, "\u2665  81 bpm", HR, F_SM)
rr(d, [18,264,342,322], 29, ORG)
t(d, W//2, 293, "LOG SET", TEX, F_LG)
rr(d, [68,330,292,358], 14, SURF)
t(d, W//2, 344, "Change Exercise", DIM, F_SM)
img.save(f"{BASE}/03_log_set.png")
print("3 done")

# ── 4. REST TIMER ─────────────────────────────────────────────
img, d = new_img()
t(d, W//2, 38, "REST", DIM, F_MD)
d.arc([28,54,332,358], -90, 270, fill=HIGH, width=14)
d.arc([28,54,332,358], -90, 126, fill=ORG,  width=14)
t(d, W//2, W//2 - 16, "54", TEX, fnt(80, bold=True))
t(d, W//2, 222, "seconds remaining", DIM, F_SM)
t(d, W//2, 244, "\u2665  88 bpm", HR, F_SM)
rr(d, [28,262,332,308], 23, SURF)
t(d, W//2, 285, "Skip Rest  \u2192", TEX, F_MD)
rr(d, [78,316,282,346], 15, (10,42,10))
t(d, W//2, 331, "Finish Workout", SUCC, F_SM)
img.save(f"{BASE}/04_rest_timer.png")
print("4 done")

# ── 5. SUMMARY ────────────────────────────────────────────────
img, d = new_img()
rr(d, [0,0,W,54], 0, SURF)
t(d, W//2, 14, "Workout Done!", SUCC, F_LG)
t(d, W//2, 56, "52m  \u2022  18 sets  \u2022  1,440 kg vol", TEX, F_SM)
t(d, W//2, 76, "25/03  18:42", DIM, F_SM)
d.line([28,90,W-28,90], fill=HIGH, width=1)
rows = [
    ("Bench Press", "4 sets  \u2022  max 82.5 kg", "80\u00d78  80\u00d78  82.5\u00d76  82.5\u00d76"),
    ("Squat",       "3 sets  \u2022  max 100 kg",  "100\u00d75  100\u00d75  100\u00d74"),
    ("OHP",         "3 sets  \u2022  max 60 kg",   "60\u00d78  60\u00d78  60\u00d77"),
]
y = 96
for name, summ, sets_ in rows:
    rr(d, [8,y,W-8,y+62], 10, SURF)
    t(d, 20, y+10, name, TEX, F_MD, "lt")
    t(d, 20, y+36, summ, DIM, F_SM, "lt")
    t(d, W-12, y+10, sets_, (68,68,85), fnt(13), "rt")
    y += 66
rr(d, [18,298,342,346], 24, SUCC)
t(d, W//2, 322, "Save Workout", TEX, F_LG)
img.save(f"{BASE}/05_summary.png")
print("5 done")

# ── 6. HISTORY ────────────────────────────────────────────────
img, d = new_img()
rr(d, [0,0,W,54], 0, SURF)
t(d, W//2, 14, "History", TEX, F_LG)
sessions = [
    ("25/03  18:42", "52m",  "18 sets  \u2022  1,440 kg",  "Bench Press, Squat, OHP"),
    ("23/03  07:15", "44m",  "15 sets  \u2022  1,120 kg",  "Pull-Ups, Barbell Row, Curl"),
    ("21/03  19:00", "61m",  "22 sets  \u2022  1,890 kg",  "Squat, Deadlift, Leg Press"),
    ("19/03  18:30", "38m",  "12 sets  \u2022  860 kg",    "Running, Jump Rope, Plank"),
]
y = 60
for date, dur, stats, exs in sessions:
    rr(d, [8,y,W-8,y+76], 12, SURF)
    t(d, 20, y+10, date, TEX, F_MD, "lt")
    t(d, W-12, y+10, dur, ORG, F_MD, "rt")
    t(d, 20, y+38, stats, DIM, F_SM, "lt")
    t(d, 20, y+56, exs, (68,68,85), fnt(13), "lt")
    y += 80
img.save(f"{BASE}/06_history.png")
print("6 done")
print("All screenshots saved to:", BASE)
