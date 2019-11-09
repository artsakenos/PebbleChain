
Super light blochchain based on free repositories and Social Networks.

Pebble
======

A block header is a 264 bit string built like this:

    [getMerkel_root()][getCreated_epoch()][getNonce()   ]
    [256bit (64Hex)  ][4bit (1 Long)     ][4bit (1 Long)]

The merkel root should always include:

* hash_previous - To validate the chain
* target - If you need to validate the target
* data - The Block data

E.g., according to the Pebble version 20191105 the merkel data (before hashing)
is shaped like this:

    version + "\n"
    + pebble.getHash_previous() + "\n"
    + pebble.getTarget() + "\n"
    + pebble.getOwner() + "\n"
    + links
    + pebble.getData() + "\n"

Securing Data
=============
If recipient is set:

* Data can be encrypted with recipient public key
* Header can eventually be also encrypted according to some version, so that only recipient can check block validity
* TODO, i'll use SuperBouncyCastle and Base64 encoding.


Directory
=========

## 0 Genesis, a Version 191105 block. 

💎caffe#caffe58c4e0e81f1b90d40dc74e186be6744f2009eb12fbb0daddaa0863e459b

Available @:

* https://pastebin.com/mUiUB2dL
* https://docs.google.com/document/d/12vLMWE1PlJQiEpYz599o6OqZ4NCOO12isqGvlBtUu00

## 2 MandatoDue Block

💎caffe2#caffe2544a6d12722d04ecc24006d9b3339e424723e97bde783d32e7056146c7

from: caffe58c4e0e81f1b90d40dc74e186be6744f2009eb12fbb0daddaa0863e459b

Available @

* Pastebin: https://pastebin.com/MWuqVru2; 
* Reddit:https://reddit.com/r/BricioleDiPane/comments/dtarkx/; 
* Youtube:https://youtu.be/VusDvPl8OJo;

See: https://twitter.com/artsakenos/status/1192677802094428160







