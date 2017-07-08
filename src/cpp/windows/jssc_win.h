/* jSSC (Java Simple Serial Connector) - serial port communication library.
* Roman Belkov <roman.belkov@gmail.com>, 2017
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
* web-site: http://scream3r.org | https://github.com/scream3r/java-simple-serial-connector
*/

#include <string>
#include <vector>

#include <jni.h>
#include <stdlib.h>
#include <windows.h>

#include <initguid.h>
#include <devguid.h>
#include <winioctl.h>
#include <setupapi.h>
#include <cfgmgr32.h>
#include <ntddmodm.h>

static std::wstring deviceRegistryProperty(HDEVINFO deviceInfoSet,
	PSP_DEVINFO_DATA deviceInfoData,
	DWORD property);

static std::wstring deviceDescription(HDEVINFO deviceInfoSet,
	PSP_DEVINFO_DATA deviceInfoData);

static std::wstring deviceManufacturer(HDEVINFO deviceInfoSet,
	PSP_DEVINFO_DATA deviceInfoData);

static std::wstring deviceInstanceIdentifier(DEVINST deviceInstanceNumber);

static DEVINST parentDeviceInstanceNumber(DEVINST childDeviceInstanceNumber);

static std::wstring parseDeviceSerialNumber(const std::wstring &instanceIdentifier);

static std::wstring deviceSerialNumber(std::wstring instanceIdentifier,
	DEVINST deviceInstanceNumber);

static uint16_t parseDeviceIdentifier(const std::wstring &instanceIdentifier,
	const std::wstring &identifierPrefix,
	int identifierSize, bool &ok);

static uint16_t deviceVendorIdentifier(const std::wstring &instanceIdentifier, bool &ok);

static uint16_t deviceProductIdentifier(const std::wstring &instanceIdentifier, bool &ok);

static bool anyOfPorts(const std::vector<std::wstring> &ports, const std::wstring &portName);

static std::wstring devicePortName(HDEVINFO deviceInfoSet, PSP_DEVINFO_DATA deviceInfoData);

// std::wstring to jstring
static jstring wstr2jstr(JNIEnv *env, std::wstring cstr);

static wchar_t *convertCharArrayToLPCWSTR(const char* charArray);

static void addWStringToJavaArray(JNIEnv *env, jobjectArray objArray, int pos, std::wstring str);

template <typename I> std::wstring intToString(I num);