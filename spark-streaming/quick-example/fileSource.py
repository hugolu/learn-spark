import os
import shutil
import time

lines = ["A B C D", "B C D E", "C D E F", "D E F A", "E F A B", "F A B C"]
idx = 0

shutil.rmtree("wordCount")
os.mkdir("wordCount")
os.chdir("wordCount")

while True:
    filename = "file-%04d.txt" % idx
    file = open(filename, "w")
    file.write(lines[idx % 6] + "\n")
    file.close()
    idx += 1
    time.sleep(0.1)
