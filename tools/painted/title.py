"""Package the painted menu composition; animation lives only in TitleBackground."""
from pathlib import Path
from PIL import Image
HERE=Path(__file__).resolve().parent

def outputs():
    background=Image.open(HERE/'sources/title-crypt.png').convert('RGBA').resize((1920,1080),Image.Resampling.LANCZOS)
    wordmark=Image.open(HERE/'sources/title-wordmark.png').convert('RGBA')
    wordmark=wordmark.crop(wordmark.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox())
    wordmark=wordmark.resize((1024,144),Image.Resampling.LANCZOS)
    mist=Image.open(HERE/'sources/title-mist.png').convert('RGBA').resize((1024,342),Image.Resampling.LANCZOS)
    if mist.getchannel('A').getextrema()[0]!=0:raise ValueError('Title mist requires genuine alpha')
    return {'interfaces/title_grimhollow.png':background,'interfaces/title_wordmark.png':wordmark,'interfaces/title_mist.png':mist}
