"""SPDX-License-Identifier: GPL-3.0-or-later. Enumerate every changed path against upstream."""
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[1]
BASE='7b8b845a76fe76c6b7c031ae9e570852411f56db'
tracked=subprocess.check_output(['git','-c','core.quotepath=false','diff','--name-only',BASE],cwd=ROOT,text=True)
untracked=subprocess.check_output(['git','ls-files','--others','--exclude-standard'],cwd=ROOT,text=True)
files=sorted(set((tracked+untracked).splitlines()) | {'CHANGES.md','tools/update-change-ledger.py'})
def reason(p):
    if p.startswith('tools/artgen/specs/'): return 'Editable deterministic source specification for its generated asset.'
    if p.endswith(('.png','.ico','.icns')): return 'Pipeline-generated palette-constrained asset; regenerate with tools/artgen/build.py.'
    if p.startswith('tools/artgen/'): return 'Deterministic procedural painter, source import, dependencies or full/subset validator.'
    if p.endswith('SmokeRun.java'): return 'Real generator/save-load smoke harness; the required missing classes fail explicitly.'
    if p.endswith('LightMapTest.java'): return 'JUnit coverage of light falloff, clipping, saturation and health thresholds.'
    if p.endswith('.gradle') or p=='gradle.properties': return 'Grimhollow build identity, platform isolation, distribution task or test configuration.'
    if p.endswith('DesktopLauncher.java'): return 'Use fork metadata for title/save isolation and expose opt-in OpenGL verification.'
    if p.endswith('DesktopSmokeProbe.java'): return 'Render title and Sewer screenshots in real OpenGL, then exit.'
    if p.endswith('GameGeometry.java'): return 'Separate 64px texture dimensions from retained logical world/UI coordinates.'
    if p.endswith('LightingOverlay.java') or p.endswith('LightMap.java'): return 'Cached low-resolution quadratic terrain light accumulation and one multiply draw.'
    if p.endswith('HealthVignette.java'): return 'Scale crimson screen edges to the specified low-health thresholds.'
    if p.endswith('NecroticParticle.java') or p.endswith('Corrosion.java'): return 'Shared green emissive corrosion particles.'
    if p.endswith('GameScene.java') or p.endswith('CharSprite.java'): return 'Integrate terrain lighting, transient level blood decals and low-health feedback.'
    if p.endswith('SPDSettings.java') or p.endswith('WndSettings.java') or '/messages/' in p: return 'Persist and localize the dynamic-lighting display setting.'
    if p.endswith('.java'): return 'Adapt terrain texture frames and central geometry constants while preserving logical placement.'
    if p.startswith('.github/'): return 'Linux/Windows desktop, Android and headless CI with artifacts and active acceptance failures.'
    if p=='GDD-one-shot-build-spec.md': return 'Preserve the exact supplied specification without credentials.'
    if p=='tools/env.ps1' or p=='tools/bootstrap-windows.ps1': return 'Reproducible local toolchain installation or session environment setup.'
    if p=='tools/github.ps1': return 'Push and inspect CI using transient authorization; never persist a credential in Git configuration.'
    if p.endswith('.md') or p.startswith('verification/'): return 'Build, pipeline, provenance or actual acceptance evidence and limitations.'
    if p=='.gitattributes': return 'Portable wrapper line endings and binary-asset treatment.'
    if p=='.gitignore': return 'Exclude machine-local toolchains, caches and temporary credentials.'
    return 'Build-support utility for this documented checkpoint.'
(ROOT/'CHANGES.md').write_text('# Changes from Shattered Pixel Dungeon v3.3.8\n\nUpstream history retained; branch `grimhollow`. Each changed path is listed below.\n\n'+''.join('- `'+p+'`: '+reason(p)+'\n' for p in files))
print(f'Listed {len(files)} changed files.')
