"""Reproducible loading-screen crops from the five original painted source images."""
from PIL import Image, ImageOps
from pack import HERE

def outputs():
    return {f'splashes/painted_region_{region}.png':
            ImageOps.fit(Image.open(HERE/'sources/regions'/f'{region}.png').convert('RGBA'),
                         (1600,900),Image.Resampling.LANCZOS,centering=(.5,.5))
            for region in ('sewers','prison','caves','city','halls')}
