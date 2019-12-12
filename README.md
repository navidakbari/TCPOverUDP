# TCP over UDP

TCP and UDP are both protocols,built on top of the Internet Protocol, used for sending bits of data known as packets.<br />
TCP, the most common protocol on the Internet, stands for Transmission Control Protocol. TCP guarantees in-order transmission of packets and makes sure the recipient recieves all the packets, in other words, TCP guarantees reliability, there is no lost or corrupted packets in TCP transmission.<br />
On the other hand, UDP is unreliable and fast. UDP stands for User Datagram Protocol, a datagram is the same as a data packet. UDP does not guarantees the Receipt of data by the reciever, this means that if a packet is lost, there is no way the reciever can claim it again, however, since all those reliability overheads are gone, it works considerably faster.<br />
## Introduction
This project will help implementing a TCP protocol using UDP transmission, students are not allowed to use tcp transmission and they are asked to implement the differences between these two protocols and transmit using udp. For example, tcp 3-way handshake or the send/recieve methods should be implemented by students themselves. 

In this project we implemented `TCP new Reno` protocol using udp. 


## Features
There is some other features beside reliable transmission in this protocol which implemented by us.

* Reliable Transfer (using `Go Back N`)
* Flow Control
* Congestion Control 
* Nagle algorithm




### Prerequisites

This project is implemented in JAVA, therefore, in order to run the codes, you need to have JAVA installed on your computer.<br />
project-jdk-name="1.8" <br />
project-jdk-type="JavaSDK" <br />

### Running the tests

in order to test your tcp send/recieve you can use Sender.java and Reciever.java.<br />

follow the instructions below:<br />

in Sender.java:
```
tcpSocket.send("the path in your computer/file");
```
in Receiver.java:
```
tcpSocket.receive("the path in your computer");
```
It is obvious that the server must be running before the client starts establishing the connection, therefore consider this when executing your code.<br />

## Built With

* [Java8](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html/) - Development Platform


## Authors

* **Farzad Habibi**
* **Navid Akbari**

