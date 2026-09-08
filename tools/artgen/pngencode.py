"""Canonical PNG serialization, including PNG payloads inside ICO/ICNS files."""
import struct
import zlib
import numpy as np
from PIL import Image

def save(image, stream, filename):
    pixels=np.asarray(image.convert('RGBA'),dtype=np.uint8)
    # Fixed Sub filtering removes Pillow's platform-dependent encoder choices.
    filtered=pixels.copy();filtered[:,1:]=pixels[:,1:]-pixels[:,:-1]
    rows=b''.join(b'\x01'+row.tobytes() for row in filtered)
    # Huffman-only encoding avoids platform-specific match-search optimizations.
    encoder=zlib.compressobj(9,zlib.DEFLATED,15,9,zlib.Z_HUFFMAN_ONLY)
    data=encoder.compress(rows)+encoder.flush()
    def chunk(kind,payload):
        stream.write(struct.pack('>I',len(payload))+kind+payload+struct.pack('>I',zlib.crc32(kind+payload)&0xffffffff))
    stream.write(b'\x89PNG\r\n\x1a\n')
    chunk(b'IHDR',struct.pack('>IIBBBBB',image.width,image.height,8,6,0,0,0))
    chunk(b'IDAT',data);chunk(b'IEND',b'')

def register():
    Image.init()
    Image.register_save('PNG',save)
