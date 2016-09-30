import random
import time
from socket import *

server = socket(AF_INET, SOCK_STREAM)
server.bind(('', 9999))
server.listen(5)

sock, addr = server.accept()
while True:
    sock.send(random.choice('ABCDEF') + "\n")
    time.sleep(0.1)

sock.close()
