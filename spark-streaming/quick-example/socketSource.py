import time
from socket import *

lines = ["A B C D", "B C D E", "C D E F", "D E F A", "E F A B", "F A B C"]
idx = 0

server = socket(AF_INET, SOCK_STREAM)
server.bind(('', 9999))
server.listen(5)

sock, addr = server.accept()
while True:
    sock.send(lines[idx % 6] + "\n")
    idx += 1
    time.sleep(0.1)

sock.close()
