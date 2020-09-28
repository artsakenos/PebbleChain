
Super light BlockChain targeted for free distribution,
e.g., on free repositories and Social Networks or printed paper.

Pebble
======

A Pebble is composed by some data and some block properties (see JavaDoc).
You just need to instantiate it according to your needs.

A block header is a 320 bit string built like this:

    [getMerkle()  ]    [256bit (64 Hex) ]
    [getCreated() ]    [4byte  (1 Long) ]
    [getNonce()   ]    [4byte  (1 Long) ]
    
A block hash is a 256bit -> 64 characters (each 4 bit / 1 hex).

The merkel root should always include:

* hash_previous     - To validate the chain
* target            - If you need to validate the target
* data              - The Block data

E.g., according to the version 20191105 the merkel data (before hashing)
is shaped like this:

    version + "\n" +
    pebble.getHash_previous() + "\n" +
    pebble.getTarget() + "\n" +
    pebble.getOwner() + "\n" +
    links +
    pebble.getData() + "\n" +


Securing Data
=============

If recipient is set:

* Data can be encrypted with recipient public key
* Header can eventually be also encrypted according to some version, so that only recipient can check block validity
* TODO, i'll use SuperBouncyCastle and Base64 encoding.

