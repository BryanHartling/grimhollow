"""Shared deforming root for future amorphous assets; no stage-6 assets are built."""
import math
from rig_biped import joint,mesh
def build(material):
    root=joint('blob',None,(0,0,0));mesh('sphere','body',root,(0,0,.2),(.25,.25,.2),material);return root
def pose(root,phase):
    wave=.07*math.sin(phase*math.tau);root.scale=(1+wave,1+wave,1-wave)
