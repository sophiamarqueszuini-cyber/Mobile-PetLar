"""
Gera uma skin com cara de iPhone 15 (moldura de titânio, Dynamic Island e
cantos arredondados) para o emulador do Android Studio.

Uso: python gerar_skin.py
A skin é salva em <SDK do Android>/skins/iphone_petlar. Depois, no Device Manager,
crie um aparelho 1180 x 2556 px, densidade 480, e escolha a skin "iphone_petlar".
"""
import os
from PIL import Image, ImageDraw

OUT = os.path.join(os.environ["LOCALAPPDATA"], "Android", "Sdk", "skins", "iphone_petlar")
os.makedirs(OUT, exist_ok=True)

SW, SH = 1180, 2556          # tela do iPhone 15 (px)
R_SCREEN = 120              # raio dos cantos da tela (não cobre o relógio do Android)
BEZEL = 40                   # borda preta
RIM = 22                     # aro de titânio
BTN = 14                     # quanto os botões saem para fora
SS = 3                       # supersampling p/ antialias

PAD = BEZEL + RIM
FW, FH = SW + 2 * PAD + 2 * BTN, SH + 2 * PAD
OX, OY = BTN + PAD, PAD      # posição da tela dentro da moldura

def big(v): return int(v * SS)

# ---------- moldura (back) ----------
im = Image.new("RGBA", (big(FW), big(FH)), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
titan = (176, 171, 163, 255)       # titânio natural
titan_hi = (215, 211, 204, 255)

# botões: esquerda (ação, volume+, volume-), direita (power)
def btn(x0, y0, x1, y1):
    d.rounded_rectangle([big(x0), big(y0), big(x1), big(y1)], radius=big(6), fill=titan)
L, R = 0, FW
btn(L, 330, BTN + 10, 430)          # botão de ação
btn(L, 560, BTN + 10, 760)          # volume +
btn(L, 820, BTN + 10, 1020)         # volume -
btn(R - BTN - 10, 640, R, 960)      # lateral

x0, y0, x1, y1 = BTN, 0, FW - BTN, FH
r_out = R_SCREEN + PAD
d.rounded_rectangle([big(x0), big(y0), big(x1), big(y1)], radius=big(r_out), fill=titan_hi)
d.rounded_rectangle([big(x0 + 4), big(y0 + 4), big(x1 - 4), big(y1 - 4)], radius=big(r_out - 4), fill=titan)
d.rounded_rectangle([big(x0 + RIM), big(y0 + RIM), big(x1 - RIM), big(y1 - RIM)],
                    radius=big(R_SCREEN + BEZEL), fill=(8, 8, 8, 255))
# recorta a área da tela (transparente)
d.rounded_rectangle([big(OX), big(OY), big(OX + SW), big(OY + SH)], radius=big(R_SCREEN), fill=(0, 0, 0, 0))
im.resize((FW, FH), Image.LANCZOS).save(os.path.join(OUT, "back.png"))

# ---------- máscara sobre a tela (cantos + Dynamic Island) ----------
m = Image.new("RGBA", (big(SW), big(SH)), (0, 0, 0, 255))
md = ImageDraw.Draw(m)
md.rounded_rectangle([0, 0, big(SW) - 1, big(SH) - 1], radius=big(R_SCREEN), fill=(0, 0, 0, 0))
IW, IH, ITOP = 378, 111, 33
ix = (SW - IW) / 2
md.rounded_rectangle([big(ix), big(ITOP), big(ix + IW), big(ITOP + IH)], radius=big(IH / 2), fill=(0, 0, 0, 255))
m.resize((SW, SH), Image.LANCZOS).save(os.path.join(OUT, "mask.png"))

with open(os.path.join(OUT, "layout"), "w", newline="\n") as f:
    f.write(f"""parts {{
  device {{
    display {{
      width {SW}
      height {SH}
      x 0
      y 0
    }}
  }}
  portrait {{
    background {{
      image back.png
    }}
    foreground {{
      mask mask.png
      cutout emu01
    }}
  }}
}}
layouts {{
  portrait {{
    width {FW}
    height {FH}
    event EV_SW:0:1
    part1 {{
      name portrait
      x 0
      y 0
    }}
    part2 {{
      name device
      x {OX}
      y {OY}
    }}
  }}
}}
""")

print("ok", OUT, FW, FH)
