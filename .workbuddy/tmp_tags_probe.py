import re, glob
out=[]
base=r"C:\Users\13475\.gradle\caches\ng_execute"
ps=glob.glob(base+r"\*\classes\net\minecraft\tags\ItemTags.class")
if ps:
    data=open(ps[0],"rb").read()
    strs=set(re.findall(rb"[\x20-\x7e]{3,}",data))
    out.append("=== ItemTags ===")
    for s in sorted(x.decode() for x in strs):
        if re.search(r"SEED|PLANT|CROP|FOOD|VILLAGER", s):
            out.append("   "+s)
ps2=glob.glob(base+r"\*\classes\net\minecraft\tags\BlockTags.class")
if ps2:
    data2=open(ps2[0],"rb").read()
    strs2=set(re.findall(rb"[\x20-\x7e]{3,}",data2))
    out.append("=== BlockTags ===")
    for s in sorted(x.decode() for x in strs2):
        if re.search(r"SEED|PLANT|CROP|VILLAGER|FLOWER|TALL", s):
            out.append("   "+s)
open(r"D:\Games\MOD\牧业\.workbuddy\tmp_tags.txt","w",encoding="utf-8").write("\n".join(out))