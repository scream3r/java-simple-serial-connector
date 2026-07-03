/* jSSC (Java Simple Serial Connector) - serial port communication library.
 * © Alexey Sokolov (scream3r), 2010-2014.
 *
 * This file is part of jSSC.
 *
 * jSSC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSSC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSSC.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you use jSSC in public project you can inform me about this by e-mail,
 * of course if you want it.
 *
 * e-mail: scream3r.org@gmail.com
 * web-site: http://scream3r.org | http://code.google.com/p/java-simple-serial-connector/
 */
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <windows.h>
#include <jssc_SerialNativeInterface.h>
#include "version.h"

//#include <iostream>

#define MAX_PORT_NAME_STR_LEN 32

/*
 * Get native library version
 */
JNIEXPORT jstring JNICALL Java_jssc_SerialNativeInterface_getNativeLibraryVersion(JNIEnv *env, jclass) {
    return env->NewStringUTF(JSSC_VERSION);
}

/*
 * Port opening.
 *
 * In 2.2.0 added useTIOCEXCL (not used in Windows, only for compatibility with _nix version)
 */
JNIEXPORT jlong JNICALL Java_jssc_SerialNativeInterface_openPort(JNIEnv *env, jobject, jstring portName, jboolean){
    char prefix[] = "\\\\.\\";
    const char* port = NULL;
    jlong retval = 0;

    port = env->GetStringUTFChars(portName, NULL);
    if( !port ){
        if( !(*env)->ExceptionCheck(env) ){
            /* Can happen if the given input is `NULL`. So let our caller know. */
            jclass exClz = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
            if( exClz ) (*env)->ThrowNew(env, exClz, "portName");
        }
        goto resolveWithRetval;
    }

    //since 2.1.0 -> string concat fix
    char portFullName[MAX_PORT_NAME_STR_LEN];

    if(strlen(prefix) + strlen(port) + 1 > sizeof(portFullName)){
        retval = (jlong)((HANDLE)jssc_SerialNativeInterface_ERR_PORT_NOT_FOUND);
        goto resolveWithRetval;
    }

    strcpy_s(portFullName, prefix);
    strcat_s(portFullName, port);
    //<- since 2.1.0

    HANDLE hComm = CreateFile(portFullName,
                       	   	  GENERIC_READ | GENERIC_WRITE,
                       	   	  0,
                       	   	  0,
                       	   	  OPEN_EXISTING,
                       	   	  FILE_FLAG_OVERLAPPED,
                       	   	  0);

    //since 2.3.0 ->
    if(hComm != INVALID_HANDLE_VALUE){
    	DCB *dcb = new DCB();
    	if(!GetCommState(hComm, dcb)){
    		CloseHandle(hComm);//since 2.7.0
    		hComm = (HANDLE)jssc_SerialNativeInterface_ERR_INCORRECT_SERIAL_PORT;//(-4)Incorrect serial port
    	}
    	delete dcb;
    }
    else {
    	DWORD errorValue = GetLastError();
    	if(errorValue == ERROR_ACCESS_DENIED){
    		hComm = (HANDLE)jssc_SerialNativeInterface_ERR_PORT_BUSY;//(-1)Port busy
    	}
    	else if(errorValue == ERROR_FILE_NOT_FOUND){
    		hComm = (HANDLE)jssc_SerialNativeInterface_ERR_PORT_NOT_FOUND;//(-2)Port not found
    	}
    }
    //<- since 2.3.0
    retval = (jlong)hComm;//since 2.4.0 changed to jlong
resolveWithRetval:
    if( port ){
        (*env)->ReleaseStringUTFChars(env, portName, port);
    }
    return retval;
}

/*
 * Setting serial port params.
 *
 * In 2.6.0 added flags (not used in Windows, only for compatibility with _nix version)
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_setParams
  (JNIEnv *, jobject, jlong portHandle, jint baudRate, jint byteSize, jint stopBits, jint parity, jboolean setRTS, jboolean setDTR, jint){
    HANDLE hComm = (HANDLE)portHandle;
    DCB *dcb = new DCB();
    jboolean returnValue = JNI_FALSE;
    if(GetCommState(hComm, dcb)){
        dcb->BaudRate = static_cast<DWORD>(baudRate);
        dcb->ByteSize = static_cast<BYTE>(byteSize);
        dcb->StopBits = static_cast<BYTE>(stopBits);
        dcb->Parity = static_cast<BYTE>(parity);

        //since 0.8 ->
        if(setRTS == JNI_TRUE){
        	dcb->fRtsControl = RTS_CONTROL_ENABLE;
        }
        else {
        	dcb->fRtsControl = RTS_CONTROL_DISABLE;
        }
        if(setDTR == JNI_TRUE){
        	dcb->fDtrControl = DTR_CONTROL_ENABLE;
        }
        else {
           	dcb->fDtrControl = DTR_CONTROL_DISABLE;
        }
        dcb->fOutxCtsFlow = FALSE;
        dcb->fOutxDsrFlow = FALSE;
        dcb->fDsrSensitivity = FALSE;
        dcb->fTXContinueOnXoff = TRUE;
        dcb->fOutX = FALSE;
        dcb->fInX = FALSE;
        dcb->fErrorChar = FALSE;
        dcb->fNull = FALSE;
        dcb->fAbortOnError = FALSE;
        dcb->XonLim = 2048;
        dcb->XoffLim = 512;
        dcb->XonChar = (char)17; //DC1
        dcb->XoffChar = (char)19; //DC3
        //<- since 0.8

        if(SetCommState(hComm, dcb)){

        	//since 2.1.0 -> timeouts set previously by another application should be cleared
        	COMMTIMEOUTS *lpCommTimeouts = new COMMTIMEOUTS();
        	lpCommTimeouts->ReadIntervalTimeout = 0;
        	lpCommTimeouts->ReadTotalTimeoutConstant = 0;
        	lpCommTimeouts->ReadTotalTimeoutMultiplier = 0;
        	lpCommTimeouts->WriteTotalTimeoutConstant = 0;
        	lpCommTimeouts->WriteTotalTimeoutMultiplier = 0;
        	if(SetCommTimeouts(hComm, lpCommTimeouts)){
        		returnValue = JNI_TRUE;
        	}
        	delete lpCommTimeouts;
        	//<- since 2.1.0
        }
    }
    delete dcb;
    return returnValue;
}

/*
 * PurgeComm
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_purgePort
  (JNIEnv *, jobject, jlong portHandle, jint flags){
    HANDLE hComm = (HANDLE)portHandle;
    DWORD dwFlags = (DWORD)flags;
    return (PurgeComm(hComm, dwFlags) ? JNI_TRUE : JNI_FALSE);
}

/*
 * Port closing
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_closePort
  (JNIEnv *, jobject, jlong portHandle){
    HANDLE hComm = (HANDLE)portHandle;
    return (CloseHandle(hComm) ? JNI_TRUE : JNI_FALSE);
}

/*
 * Set events mask
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_setEventsMask
  (JNIEnv *, jobject, jlong portHandle, jint mask){
    HANDLE hComm = (HANDLE)portHandle;
    DWORD dwEvtMask = (DWORD)mask;
    return (SetCommMask(hComm, dwEvtMask) ? JNI_TRUE : JNI_FALSE);
}

/*
 * Get events mask
 */
JNIEXPORT jint JNICALL Java_jssc_SerialNativeInterface_getEventsMask
  (JNIEnv *, jobject, jlong portHandle){
    HANDLE hComm = (HANDLE)portHandle;
    DWORD lpEvtMask;
    if(GetCommMask(hComm, &lpEvtMask)){
        return (jint)lpEvtMask;
    }
    else {
        return -1;
    }
}

/*
 * Change RTS line state (ON || OFF)
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_setRTS
  (JNIEnv *, jobject, jlong portHandle, jboolean enabled){
    HANDLE hComm = (HANDLE)portHandle;
    if(enabled == JNI_TRUE){
        return (EscapeCommFunction(hComm, SETRTS) ? JNI_TRUE : JNI_FALSE);
    }
    else {
        return (EscapeCommFunction(hComm, CLRRTS) ? JNI_TRUE : JNI_FALSE);
    }
}

/*
 * Change DTR line state (ON || OFF)
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_setDTR
  (JNIEnv *, jobject, jlong portHandle, jboolean enabled){
    HANDLE hComm = (HANDLE)portHandle;
    if(enabled == JNI_TRUE){
        return (EscapeCommFunction(hComm, SETDTR) ? JNI_TRUE : JNI_FALSE);
    }
    else {
        return (EscapeCommFunction(hComm, CLRDTR) ? JNI_TRUE : JNI_FALSE);
    }
}

/*
 * Write data to port
 * portHandle - port handle
 * buffer - byte array for sending
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_writeBytes
  (JNIEnv *env, jobject, jlong portHandle, jbyteArray buffer){
    HANDLE hComm = (HANDLE)portHandle;
    DWORD lpNumberOfBytesTransferred;
    DWORD lpNumberOfBytesWritten;
    jboolean returnValue = JNI_FALSE;
    if( buffer == NULL ){
        jclass exClz = env->FindClass("java/lang/NullPointerException");
        if( exClz != NULL ) env->ThrowNew(exClz, "buffer");
        return 0;
    }
    jbyte* jBuffer = env->GetByteArrayElements(buffer, JNI_FALSE);
    if( jBuffer == NULL ){
        jclass exClz = env->FindClass("java/lang/RuntimeException");
        if( exClz != NULL ) env->ThrowNew(exClz, "jni->GetByteArrayElements() failed");
        return 0;
    }
    OVERLAPPED *overlapped = new OVERLAPPED();
    overlapped->hEvent = CreateEventA(NULL, true, false, NULL);
    if(WriteFile(hComm, jBuffer, (DWORD)env->GetArrayLength(buffer), &lpNumberOfBytesWritten, overlapped)){
        returnValue = JNI_TRUE;
    }
    else if(GetLastError() == ERROR_IO_PENDING){
        if(WaitForSingleObject(overlapped->hEvent, INFINITE) == WAIT_OBJECT_0){
            if(GetOverlappedResult(hComm, overlapped, &lpNumberOfBytesTransferred, false)){
                returnValue = JNI_TRUE;
            }
        }
    }
    env->ReleaseByteArrayElements(buffer, jBuffer, 0);
    CloseHandle(overlapped->hEvent);
    delete overlapped;
    return returnValue;
}

/*
 * Read data from port
 * portHandle - port handle
 * byteCount - count of bytes for reading
 */
JNIEXPORT jbyteArray JNICALL Java_jssc_SerialNativeInterface_readBytes
  (JNIEnv *env, jobject, jlong portHandle, jint byteCount){
    HANDLE hComm = (HANDLE)portHandle;
    DWORD lpNumberOfBytesTransferred;
    DWORD lpNumberOfBytesRead;
    jbyteArray returnArray = NULL;
    jbyte *lpBuffer = NULL;
    OVERLAPPED *overlapped = NULL;

    if( byteCount < 0 ){
        char emsg[64]; emsg[0] = '\0';
        snprintf(emsg, sizeof emsg, "byteCount %d. Expected range: 0..2147483647", byteCount);
        jclass exClz = env->FindClass("java/lang/IllegalArgumentException");
        if( exClz ) env->ThrowNew(exClz, emsg);
        returnArray = NULL; goto Finally;
    }else if( byteCount == 0 ){
        returnArray = env->NewByteArray(0);
        goto Finally;
    }

    returnArray = env->NewByteArray(byteCount);
    if( returnArray == NULL ) goto Finally;

    lpBuffer = (jbyte*)malloc(byteCount*sizeof*lpBuffer);
    if( !lpBuffer ){
        char emsg[32]; emsg[0] = '\0';
        snprintf(emsg, sizeof emsg, "malloc(%d) failed", byteCount*sizeof*lpBuffer);
        jclass exClz = env->FindClass("java/lang/RuntimeException");
        if( exClz ) env->ThrowNew(exClz, emsg);
        returnArray = NULL; goto Finally;
    }

    overlapped = new OVERLAPPED();
    overlapped->hEvent = CreateEventA(NULL, true, false, NULL);
    if(ReadFile(hComm, lpBuffer, (DWORD)byteCount, &lpNumberOfBytesRead, overlapped)){
        env->SetByteArrayRegion(returnArray, 0, byteCount, lpBuffer);
    }
    else if(GetLastError() == ERROR_IO_PENDING){
        if(WaitForSingleObject(overlapped->hEvent, INFINITE) == WAIT_OBJECT_0){
            if(GetOverlappedResult(hComm, overlapped, &lpNumberOfBytesTransferred, false)){
                env->SetByteArrayRegion(returnArray, 0, byteCount, lpBuffer);
            }
        }
    }
    else if(GetLastError() == ERROR_INVALID_HANDLE){
        jclass exClz = env->FindClass("java/lang/IllegalArgumentException");
        if( exClz != NULL ) env->ThrowNew(exClz, "EBADF");
    }
Finally:
    if( overlapped ){
        CloseHandle(overlapped->hEvent);
        delete overlapped;
    }
    if( lpBuffer ) free(lpBuffer);
    return returnArray;
}

/*
 * Get bytes count in serial port buffers (Input and Output)
 */
JNIEXPORT jintArray JNICALL Java_jssc_SerialNativeInterface_getBuffersBytesCount
  (JNIEnv *env, jobject, jlong portHandle){
	HANDLE hComm = (HANDLE)portHandle;
	jint returnValues[2];
	returnValues[0] = -1;
	returnValues[1] = -1;
	jintArray returnArray = env->NewIntArray(2);
	DWORD lpErrors;
	COMSTAT *comstat = new COMSTAT();
	if(ClearCommError(hComm, &lpErrors, comstat)){
		returnValues[0] = (jint)comstat->cbInQue;
		returnValues[1] = (jint)comstat->cbOutQue;
	}
	else {
		returnValues[0] = -1;
		returnValues[1] = -1;
	}
	delete comstat;
	env->SetIntArrayRegion(returnArray, 0, 2, returnValues);
	return returnArray;
}

//since 0.8 ->
const jint FLOWCONTROL_NONE = 0;
const jint FLOWCONTROL_RTSCTS_IN = 1;
const jint FLOWCONTROL_RTSCTS_OUT = 2;
const jint FLOWCONTROL_XONXOFF_IN = 4;
const jint FLOWCONTROL_XONXOFF_OUT = 8;
//<- since 0.8

/*
 * Setting flow control mode
 *
 * since 0.8
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_setFlowControlMode
  (JNIEnv *, jobject, jlong portHandle, jint mask){
	HANDLE hComm = (HANDLE)portHandle;
	jboolean returnValue = JNI_FALSE;
	DCB *dcb = new DCB();
	if(GetCommState(hComm, dcb)){
		dcb->fRtsControl = RTS_CONTROL_ENABLE;
		dcb->fOutxCtsFlow = FALSE;
		dcb->fOutX = FALSE;
		dcb->fInX = FALSE;
		if(mask != FLOWCONTROL_NONE){
			if((mask & FLOWCONTROL_RTSCTS_IN) == FLOWCONTROL_RTSCTS_IN){
				dcb->fRtsControl = RTS_CONTROL_HANDSHAKE;
			}
			if((mask & FLOWCONTROL_RTSCTS_OUT) == FLOWCONTROL_RTSCTS_OUT){
				dcb->fOutxCtsFlow = TRUE;
			}
			if((mask & FLOWCONTROL_XONXOFF_IN) == FLOWCONTROL_XONXOFF_IN){
				dcb->fInX = TRUE;
			}
			if((mask & FLOWCONTROL_XONXOFF_OUT) == FLOWCONTROL_XONXOFF_OUT){
				dcb->fOutX = TRUE;
			}
		}
		if(SetCommState(hComm, dcb)){
			returnValue = JNI_TRUE;
		}
	}
	delete dcb;
	return returnValue;
}

/*
 * Getting flow control mode
 *
 * since 0.8
 */
JNIEXPORT jint JNICALL Java_jssc_SerialNativeInterface_getFlowControlMode
  (JNIEnv *, jobject, jlong portHandle){
	HANDLE hComm = (HANDLE)portHandle;
	jint returnValue = 0;
	DCB *dcb = new DCB();
	if(GetCommState(hComm, dcb)){
		if(dcb->fRtsControl == RTS_CONTROL_HANDSHAKE){
			returnValue |= FLOWCONTROL_RTSCTS_IN;
		}
		if(dcb->fOutxCtsFlow == TRUE){
			returnValue |= FLOWCONTROL_RTSCTS_OUT;
		}
		if(dcb->fInX == TRUE){
			returnValue |= FLOWCONTROL_XONXOFF_IN;
		}
		if(dcb->fOutX == TRUE){
			returnValue |= FLOWCONTROL_XONXOFF_OUT;
		}
	}
	delete dcb;
	return returnValue;
}

/*
 * Send break for set duration
 *
 * since 0.8
 */
JNIEXPORT jboolean JNICALL Java_jssc_SerialNativeInterface_sendBreak
  (JNIEnv *, jobject, jlong portHandle, jint duration){
	HANDLE hComm = (HANDLE)portHandle;
	jboolean returnValue = JNI_FALSE;
	if(duration > 0){
		if(SetCommBreak(hComm) > 0){
			Sleep(duration);
			if(ClearCommBreak(hComm) > 0){
				returnValue = JNI_TRUE;
			}
		}
	}
	return returnValue;
}


/*
 * Wait event
 * portHandle - port handle
 */
JNIEXPORT jobjectArray JNICALL Java_jssc_SerialNativeInterface_waitEvents
  ( JNIEnv*env, jobject, jlong portHandle, jint/*unused on windows*/ ){
    HANDLE hComm = (HANDLE)portHandle;
    DWORD lpEvtMask = 0;
    DWORD lpNumberOfBytesTransferred = 0;
    OVERLAPPED *overlapped = new OVERLAPPED();
    jclass intClass = env->FindClass("[I");
    jobjectArray returnArray;
    boolean functionSuccessful = false;
    overlapped->hEvent = CreateEventA(NULL, true, false, NULL);
    if(WaitCommEvent(hComm, &lpEvtMask, overlapped)){
        functionSuccessful = true;
    }
    else if(GetLastError() == ERROR_IO_PENDING){
        if(WaitForSingleObject(overlapped->hEvent, INFINITE) == WAIT_OBJECT_0){
            if(GetOverlappedResult(hComm, overlapped, &lpNumberOfBytesTransferred, false)){
                functionSuccessful = true;
            }
        }
    }
    if(functionSuccessful){
        boolean executeGetCommModemStatus = false;
        boolean executeClearCommError = false;
        DWORD events[9];//fixed since 0.8 (old value is 8)
        jint eventsCount = 0;
        if((EV_BREAK & lpEvtMask) == EV_BREAK){
            events[eventsCount] = EV_BREAK;
            eventsCount++;
        }
        if((EV_CTS & lpEvtMask) == EV_CTS){
            events[eventsCount] = EV_CTS;
            eventsCount++;
            executeGetCommModemStatus = true;
        }
        if((EV_DSR & lpEvtMask) == EV_DSR){
            events[eventsCount] = EV_DSR;
            eventsCount++;
            executeGetCommModemStatus = true;
        }
        if((EV_ERR & lpEvtMask) == EV_ERR){
            events[eventsCount] = EV_ERR;
            eventsCount++;
            executeClearCommError = true;
        }
        if((EV_RING & lpEvtMask) == EV_RING){
            events[eventsCount] = EV_RING;
            eventsCount++;
            executeGetCommModemStatus = true;
        }
        if((EV_RLSD & lpEvtMask) == EV_RLSD){
            events[eventsCount] = EV_RLSD;
            eventsCount++;
            executeGetCommModemStatus = true;
        }
        if((EV_RXCHAR & lpEvtMask) == EV_RXCHAR){
            events[eventsCount] = EV_RXCHAR;
            eventsCount++;
            executeClearCommError = true;
        }
        if((EV_RXFLAG & lpEvtMask) == EV_RXFLAG){
            events[eventsCount] = EV_RXFLAG;
            eventsCount++;
            executeClearCommError = true;
        }
        if((EV_TXEMPTY & lpEvtMask) == EV_TXEMPTY){
            events[eventsCount] = EV_TXEMPTY;
            eventsCount++;
            executeClearCommError = true;
        }
        /*
         * Execute GetCommModemStatus function if it's needed (get lines status)
         */
        jint statusCTS = 0;
        jint statusDSR = 0;
        jint statusRING = 0;
        jint statusRLSD = 0;
        boolean successGetCommModemStatus = false;
        if(executeGetCommModemStatus){
            DWORD lpModemStat;
            if(GetCommModemStatus(hComm, &lpModemStat)){
                successGetCommModemStatus = true;
                if((MS_CTS_ON & lpModemStat) == MS_CTS_ON){
                    statusCTS = 1;
                }
                if((MS_DSR_ON & lpModemStat) == MS_DSR_ON){
                    statusDSR = 1;
                }
                if((MS_RING_ON & lpModemStat) == MS_RING_ON){
                    statusRING = 1;
                }
                if((MS_RLSD_ON & lpModemStat) == MS_RLSD_ON){
                    statusRLSD = 1;
                }
            }
            else {
                jint lastError = (jint)GetLastError();
                statusCTS = lastError;
                statusDSR = lastError;
                statusRING = lastError;
                statusRLSD = lastError;
            }
        }
        /*
         * Execute ClearCommError function if it's needed (get count of bytes in buffers and errors)
         */
        jint bytesCountIn = 0;
        jint bytesCountOut = 0;
        jint communicationsErrors = 0;
        boolean successClearCommError = false;
        if(executeClearCommError){
            DWORD lpErrors;
            COMSTAT *comstat = new COMSTAT();
            if(ClearCommError(hComm, &lpErrors, comstat)){
                successClearCommError = true;
                bytesCountIn = (jint)comstat->cbInQue;
                bytesCountOut = (jint)comstat->cbOutQue;
                communicationsErrors = (jint)lpErrors;
            }
            else {
                jint lastError = (jint)GetLastError();
                bytesCountIn = lastError;
                bytesCountOut = lastError;
                communicationsErrors = lastError;
            }
            delete comstat;
        }
        /*
         * Create int[][] for events values
         */
        returnArray = env->NewObjectArray(eventsCount, intClass, NULL);
        /*
         * Set events values
         */
        for(jint i = 0; i < eventsCount; i++){
            jint returnValues[2];
            switch(events[i]){
                case EV_BREAK:
                    returnValues[0] = (jint)events[i];
                    returnValues[1] = 0;
                    goto forEnd;
                case EV_CTS:
                    returnValues[1] = statusCTS;
                    goto modemStatus;
                case EV_DSR:
                    returnValues[1] = statusDSR;
                    goto modemStatus;
                case EV_ERR:
                    returnValues[1] = communicationsErrors;
                    goto bytesAndErrors;
                case EV_RING:
                    returnValues[1] = statusRING;
                    goto modemStatus;
                case EV_RLSD:
                    returnValues[1] = statusRLSD;
                    goto modemStatus;
                case EV_RXCHAR:
                    returnValues[1] = bytesCountIn;
                    goto bytesAndErrors;
                case EV_RXFLAG:
                    returnValues[1] = bytesCountIn;
                    goto bytesAndErrors;
                case EV_TXEMPTY:
                    returnValues[1] = bytesCountOut;
                    goto bytesAndErrors;
                default:
                    returnValues[0] = (jint)events[i];
                    returnValues[1] = 0;
                    goto forEnd;
            };
            modemStatus: {
                if(successGetCommModemStatus){
                    returnValues[0] = (jint)events[i];
                }
                else {
                    returnValues[0] = -1;
                }
                goto forEnd;
            }
            bytesAndErrors: {
                if(successClearCommError){
                    returnValues[0] = (jint)events[i];
                }
                else {
                    returnValues[0] = -1;
                }
                goto forEnd;
            }
            forEnd: {
                jintArray singleResultArray = env->NewIntArray(2);
                env->SetIntArrayRegion(singleResultArray, 0, 2, returnValues);
                env->SetObjectArrayElement(returnArray, i, singleResultArray);
            };
        }
    }
    else {
        returnArray = env->NewObjectArray(1, intClass, NULL);
        jint returnValues[2];
        returnValues[0] = -1;
        returnValues[1] = (jint)GetLastError();
        jintArray singleResultArray = env->NewIntArray(2);
        env->SetIntArrayRegion(singleResultArray, 0, 2, returnValues);
        env->SetObjectArrayElement(returnArray, 0, singleResultArray);
    };
    CloseHandle(overlapped->hEvent);
    delete overlapped;
    return returnArray;
}

/*
 * Get serial port names
 */
JNIEXPORT jobjectArray JNICALL Java_jssc_SerialNativeInterface_getSerialPortNames
  (JNIEnv *env, jobject){
    HKEY phkResult = NULL;
    LPCSTR lpSubKey = "HARDWARE\\DEVICEMAP\\SERIALCOMM\\";
    jobjectArray returnArray = NULL;
    byte lpDataOnStack[256];
    byte *lpData = lpDataOnStack;
    DWORD lpData_capacity = 256;
    char valueName[256];
    DWORD valueNameSize;
    DWORD enumResult;
    boolean hasMoreElements = true;
    DWORD keysCount = 0;
    if(RegOpenKeyExA(HKEY_LOCAL_MACHINE, lpSubKey, 0, KEY_READ, &phkResult) != ERROR_SUCCESS){
        returnArray = NULL;
        goto Finally;
    }
    /* Iterate a 1st time to see how long our resulting array has to be. */
    while(hasMoreElements){
        valueNameSize = 256;
        enumResult = RegEnumValueA(phkResult, keysCount, valueName, &valueNameSize, NULL, NULL, NULL, NULL);
        if(enumResult == ERROR_SUCCESS){
            keysCount++;
        }
        else if(enumResult == ERROR_NO_MORE_ITEMS){
            hasMoreElements = false;
        }
        else {
            hasMoreElements = false;
        }
    }
    if(keysCount > 0){
        jclass stringClass = env->FindClass("java/lang/String");
        returnArray = env->NewObjectArray((jsize)keysCount, stringClass, NULL);
        DWORD lpcbData;
        DWORD result;
        /* iterate 2nd time but this time our array is ready to catch results. */
        for(DWORD i = 0; i < keysCount; i++){
            valueNameSize = 256;
            lpcbData = lpData_capacity;
            result = RegEnumValueA(phkResult, i, valueName, &valueNameSize, NULL, NULL, lpData, &lpcbData);
            if(result == ERROR_SUCCESS){
                env->SetObjectArrayElement(returnArray, i, env->NewStringUTF((char*)lpData));
            }else if(result == ERROR_MORE_DATA && lpData_capacity < UINT_MAX){
                /* whoops our supplied buffer was not large enough. Fallback to a heap
                 * allocd one. RegEnumValueA was kind enough to set 'lpcbData' to the
                 * required length before return. */
                lpData = (lpData == lpDataOnStack) ? NULL : lpData;
                /* MS doc is unclear to me if returned size includes the required
                 * zero-term. So to stay safe, we provide one byte more. */
                if(lpcbData < UINT_MAX) lpcbData += 1;
                byte *tmp = (byte*)realloc(lpData, lpcbData*sizeof*tmp);
                if(tmp == NULL){
                    jclass exClz = env->FindClass("java/lang/OutOfMemoryError");
                    if(exClz != NULL) env->ThrowNew(exClz, NULL);
                    returnArray = NULL;
                    goto Finally;
                }
                /* install new buffer and reset 'i' so we try the same element again. */
                lpData_capacity = lpcbData;
                lpData = tmp;
                i -= 1;
            }else{
                char exMsg[128];
                snprintf(exMsg, sizeof exMsg, "RegEnumValueA(): Code %ld: "
                    "https://learn.microsoft.com/en-us/windows/win32/debug/system-error-codes#system-error-codes",
                    result);
                jclass exClz = env->FindClass("java/lang/RuntimeException");
                if(exClz != NULL) env->ThrowNew(exClz, exMsg);
                returnArray = NULL;
                goto Finally;
            }
        }
    }
Finally:
    if(lpData != NULL && lpData != lpDataOnStack) free(lpData);
    if(phkResult != NULL) CloseHandle(phkResult);
    return returnArray;
}

/*
 * Get lines status
 *
 * returnValues[0] - CTS
 * returnValues[1] - DSR
 * returnValues[2] - RING
 * returnValues[3] - RLSD
 *
 */
JNIEXPORT jintArray JNICALL Java_jssc_SerialNativeInterface_getLinesStatus
  (JNIEnv *env, jobject, jlong portHandle){
    HANDLE hComm = (HANDLE)portHandle;
    DWORD lpModemStat;
    jint returnValues[4];
    for(jint i = 0; i < 4; i++){
        returnValues[i] = 0;
    }
    jintArray returnArray = env->NewIntArray(4);
    if(GetCommModemStatus(hComm, &lpModemStat)){
        if((MS_CTS_ON & lpModemStat) == MS_CTS_ON){
            returnValues[0] = 1;
        }
        if((MS_DSR_ON & lpModemStat) == MS_DSR_ON){
            returnValues[1] = 1;
        }
        if((MS_RING_ON & lpModemStat) == MS_RING_ON){
            returnValues[2] = 1;
        }
        if((MS_RLSD_ON & lpModemStat) == MS_RLSD_ON){
            returnValues[3] = 1;
        }
    }
    env->SetIntArrayRegion(returnArray, 0, 4, returnValues);
    return returnArray;
}
