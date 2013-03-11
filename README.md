jSSC-0.9.0 Release version (21.12.2011)

This version contains native libs for Windows(x86, x86-64), Linux(x86, x86-64), Solaris(x86, x86-64), Mac OS X(x86, x86-64, PPC, PPC64).
All native libs contains in the jssc.jar file and you don't need manage native libs manually.

In this build:
* Added Solaris support (x86, x86-64)
* Added Mac OS X support 10.5 and higher(x86, x86-64, PPC, PPC64)
* Fixed some bugs in Linux native part
* Changed openPort() method

Important Note:
	openPort() method has been changed, now if port busy SerialPortException with type: TYPE_PORT_BUSY will be thrown, 
	and if port not found SerialPortException with type: TYPE_PORT_NOT_FOUND will be thrown. 
	
	It's possible to know that port is busy (TYPE_PORT_BUSY) by using TIOCEXCL directive in *nix native library, 
	but using of this directive make some troubles in Solaris OS, that's why TIOCEXCL not used in Solaris (!)
	Be careful with it.
	
	Also Solaris and Mac OS X versions of jSSC not support following events:
	ERR, TXEMPTY, BREAK.
	
	Solaris version not support non standard baudrates
	Mac OS X version not support parity: MARK, SPACE.

* Included javadoc and source codes

With Best Regards, Sokolov Alexey.

============= Previous Builds ==============

/////////////////////////////////////////
//jSSC-0.8 Release version (28.11.2011)//
/////////////////////////////////////////

In this build:
* Implemented events BREAK and ERR (RXFLAG not supported in Linux)
* Added method sendBreak(int duration) - send Break signal for setted time
* Fixed bugs in Linux events listener
* Fixed bug with long port closing operation in Linux

/////////////////////////////
//jSSC-0.8-tb4 (21.11.2011)//
/////////////////////////////

In this build was fixed a bug in getPortNames() method under Linux.

Not implemented yet list:
* Events: BREAK, ERR and RXFLAG

/////////////////////////////
//jSSC-0.8-tb3 (09.09.2011)//
/////////////////////////////

In this build was implemented:
* purgePort()

And was fixed some Linux and Windows lib bugs.

New in this build:
* getInputBufferBytesCount() - get count of bytes in input buffer (if error has occured -1 will be returned)
* getOutputBufferBytesCount() - get count of bytes in output buffer (if error has occured -1 will be returned)
* setFlowControlMode() - setting flow control (available: FLOWCONTROL_NONE, 
                                                          FLOWCONTROL_RTSCTS_IN,
                                                          FLOWCONTROL_RTSCTS_OUT,
                                                          FLOWCONTROL_XONXOFF_IN,
                                                          FLOWCONTROL_XONXOFF_OUT)
* getFlowControlMode() - getting setted flow control mode

Some "syntactic sugar" for more usability:

* writeByte() - write single byte
* writeString() - write string
* writeInt() - write int value (for example 0xFF)
* writeIntArray - write int array (for example new int[]{0xFF, 0x00, 0xFF})

* readString(int byteCount) - read string
* readHexString(int byteCount) - read Hex string with a space separator (for example "FF 00 FF")
* readHexString(int byteCount, String separator) - read Hex string with setted separator (for example if separator : "FF:00:FF")
* readHexStringArray(int byteCount) - read Hex string array (for example {FF, 00, FF})
* readIntArray(int byteCount) - read int array (values in int array are in range from 0 to 255
                                                for example if byte == -1 value in this array it will be 255)

The following methods read all bytes in input buffer, if buffer is empty methods will return null

* readBytes()
* readString()
* readHexString()
* readHexString()
* readHexStringArray()
* readIntArray()

============================================

Not implemented yet list:
* Events: BREAK, ERR and RXFLAG

///////////////////////////////
// jSSC-0.8-tb2 (14.07.2011) //
///////////////////////////////

In this build was implemented:
* getPortNames()
* Parity: MARK and SPACE

And was fixed some Linux lib bugs.

Not implemented yet list:
* purgePort()
* Events: BREAK, ERR and RXFLAG

///////////////////////////////
// jSSC-0.8-tb1 (11.07.2011) //
///////////////////////////////

Not implemented yet list:
* getPortNames()
* Parity: MARK and SPACE
* purgePort()
* Events: BREAK, ERR and RXFLAG
