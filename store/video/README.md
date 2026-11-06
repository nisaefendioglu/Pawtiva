# Promo videos

- `pawtiva-promo-en.mp4` — English, 53.5 s, 1920×1080, 30 fps, no audio
- `pawtiva-tanitim.mp4` — Turkish, same cut

Animated explainers made from the app's own pet artwork and palette (not screen recordings).
Every feature shown exists in the app.

Regenerate (needs Python 3, Playwright with Chromium, ffmpeg, Inter and Noto Color Emoji fonts):

```bash
python3 build.py tr && python3 render.py tr   # Turkish
python3 build.py en && python3 render.py en   # English
```
