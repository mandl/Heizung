/*
    Heizung
    
    Copyright (C) 2016 Mandl

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <linux/ioctl.h>
#include <android/log.h>
#include <linux/i2c.h>

#define DEBUG_TAG "NDK_STEUERUNG"

#define GPIO_IOC_MAGIC 0x90

#define GPIO_IOCQMODE           _IOR(GPIO_IOC_MAGIC, 0x01, uint32_t)
#define GPIO_IOCTMODE0          _IOW(GPIO_IOC_MAGIC, 0x02, uint32_t)
#define GPIO_IOCTMODE1          _IOW(GPIO_IOC_MAGIC, 0x03, uint32_t)
#define GPIO_IOCTMODE2          _IOW(GPIO_IOC_MAGIC, 0x04, uint32_t)
#define GPIO_IOCTMODE3          _IOW(GPIO_IOC_MAGIC, 0x05, uint32_t)

#define GPIO_IOCQDIR            _IOR(GPIO_IOC_MAGIC, 0x06, uint32_t)
#define GPIO_IOCSDIRIN          _IOW(GPIO_IOC_MAGIC, 0x07, uint32_t)
#define GPIO_IOCSDIROUT         _IOW(GPIO_IOC_MAGIC, 0x08, uint32_t)

#define GPIO_IOCQPULLEN         _IOR(GPIO_IOC_MAGIC, 0x09, uint32_t)
#define GPIO_IOCSPULLENABLE     _IOW(GPIO_IOC_MAGIC, 0x0A, uint32_t)
#define GPIO_IOCSPULLDISABLE    _IOW(GPIO_IOC_MAGIC, 0x0B, uint32_t)
#define GPIO_IOCQPULL           _IOR(GPIO_IOC_MAGIC, 0x0C, uint32_t)
#define GPIO_IOCSPULLDOWN       _IOW(GPIO_IOC_MAGIC, 0x0D, uint32_t)
#define GPIO_IOCSPULLUP         _IOW(GPIO_IOC_MAGIC, 0x0E, uint32_t)

#define GPIO_IOCQINV            _IOR(GPIO_IOC_MAGIC, 0x0F, uint32_t)
#define GPIO_IOCSINVENABLE      _IOW(GPIO_IOC_MAGIC, 0x10, uint32_t)
#define GPIO_IOCSINVDISABLE     _IOW(GPIO_IOC_MAGIC, 0x11, uint32_t)
#define GPIO_IOCQDATAIN         _IOR(GPIO_IOC_MAGIC, 0x12, uint32_t)
#define GPIO_IOCQDATAOUT        _IOR(GPIO_IOC_MAGIC, 0x13, uint32_t)
#define GPIO_IOCSDATALOW        _IOW(GPIO_IOC_MAGIC, 0x14, uint32_t)
#define GPIO_IOCSDATAHIGH       _IOW(GPIO_IOC_MAGIC, 0x15, uint32_t)

#define PMIC_MAGIC              0x6B
#define PMIC_TEST               _IO(PMIC_MAGIC, 0x00)
#define PMIC_READ               _IOW(PMIC_MAGIC, 0x01, uint32_t)
#define PMIC_WRITE              _IOW(PMIC_MAGIC, 0x02, uint32_t)
#define SET_PMIC_LCDBK 3
#define ENABLE_VIBRATOR         _IO(PMIC_MAGIC, 0x04)
#define DISABLE_VIBRATOR        _IO(PMIC_MAGIC, 0x05)

// (0x21) LDO CTRL 8 V3GTX
#define V3GTX_SEL_MASK					0x3
#define V3GTX_SEL_SHIFT					0x0
#define V3GTX_ICAL_EN_MASK				0x3
#define V3GTX_ICAL_EN_SHIFT			0x2
#define V3GTX_CAL_MASK					0xF
#define V3GTX_CAL_SHIFT					0x4
// (0x22) LDO CTRL 9 V3GTX
#define V3GTX_CALST_MASK				0x3
#define V3GTX_CALST_SHIFT				0x0
#define V3GTX_CALOC_MASK				0x3
#define V3GTX_CALOC_SHIFT				0x2
#define V3GTX_OC_AUTO_OFF_MASK		0x1
#define V3GTX_OC_AUTO_OFF_SHIFT		0x4
#define V3GTX_EN_MASK					0x1
#define V3GTX_EN_SHIFT					0x5
#define V3GTX_ON_SEL_MASK				0x1
#define V3GTX_ON_SEL_SHIFT				0x6
#define V3GTX_EN_FORCE_MASK			0x1
#define V3GTX_EN_FORCE_SHIFT			0x7

int fd = 0;

jint
Java_com_steuerung_heizung_SteuerungService_serialopen(JNIEnv* env,
    jobject thiz)
{
  int n, i;

  struct termios toptions;

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "serialopen");

  /* open serial port */
  fd = open("/dev/ttyMT1", O_RDWR | O_NOCTTY);
  if (fd < 0)
    {
      __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG,
          "open fail /dev/ttyMT1 %d", errno);
      return -errno;
    }


  /* get current serial port settings */
  tcgetattr(fd, &toptions);
  /* set 115200 baud both ways */
  cfsetispeed(&toptions, B9600);
  cfsetospeed(&toptions, B9600);
  /* 8 bits, no parity, no stop bits */
  toptions.c_cflag &= ~PARENB;
  toptions.c_cflag &= ~CSTOPB;
  toptions.c_cflag &= ~CSIZE;
  toptions.c_cflag |= CS8;
  /* Canonical mode */
  toptions.c_lflag |= ICANON;
  toptions.c_lflag &= ~(ECHO | ECHONL | IEXTEN | ISIG);

  //termios.c_lflag &= ~ICANON; /* Set non-canonical mode */
  //termios.c_cc[VTIME] = 100; /* Set timeout of 10.0 seconds */
  /* commit the serial port settings */
  tcsetattr(fd, TCSANOW, &toptions);

  return fd;

}
jint
Java_com_steuerung_heizung_SteuerungService_serialwrite(JNIEnv* env,
    jobject thiz, jstring command)
{
  int rc;
  const char *nativeString = (*env)->GetStringUTFChars(env, command, 0);
  int len;

  len = strlen(nativeString);

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "Serial: %s", nativeString);

  rc = write(fd, nativeString, len);

  (*env)->ReleaseStringUTFChars(env, command, nativeString);

  if (rc != len)
  {
      __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "write error");
      return -1;
  }

  return 0;
}

jstring
Java_com_steuerung_heizung_SteuerungService_serialread(JNIEnv* env,
    jobject thiz)
{
  char buf[80];
  int rc;

  /* Receive string */
  rc = read(fd, buf, sizeof(buf));

  if (rc == -1)
    {
      __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "read error");
      return;
    }
  if (rc > 0)
    {
      buf[rc - 1] = 0;   // remove newline
      __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "Serial read: %s", buf);
      return (*env)->NewStringUTF(env, buf);
    }

  return (*env)->NewStringUTF(env, "");
}

void
Java_com_steuerung_heizung_SteuerungService_pmicon(JNIEnv* env, jobject thiz)
{
  int rc2 = 0, fd = 0;
  int i;

  uint32_t data[2];
  uint32_t datareg;

  char buf[10] =
    { 0 };

  //__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "pmic");

  fd = open("/dev/MT6326-pmic", O_RDWR);
  if (fd < 0)
    {

      __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG,
          "/dev/MT6326-pmic open fail %d", fd);

      return;
    }

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "/dev/MT6326-pmic open  %d",
      fd);

  datareg = 0x21;

  rc2 = ioctl(fd, PMIC_READ, &datareg);

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "read  %d", datareg);

  /*

   data[1] = datareg;
   data[1] &= ~(V3GTX_CAL_MASK << (V3GTX_CAL_SHIFT));
   data[1] |= (0 << (V3GTX_CAL_SHIFT));
   data[0] = 0x21;
   rc2 = ioctl(fd, PMIC_WRITE, data);

   // 2_8 Volt
   //data[1] = 0;
   data[1] &= ~(V3GTX_SEL_MASK << (V3GTX_SEL_SHIFT));
   data[1] |= (0 << (V3GTX_SEL_SHIFT));
   data[0] = 0x21;
   rc2 = ioctl(fd, PMIC_WRITE, data);
   */
  datareg = 0x22;

  rc2 = ioctl(fd, PMIC_READ, &datareg);

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "read 0x22  %d", datareg);

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "pmic read  %d", data);

  /*
   data[1] = datareg;
   data[1] &= ~(V3GTX_ON_SEL_MASK << (V3GTX_ON_SEL_SHIFT));
   data[1] |= (1 << (V3GTX_ON_SEL_SHIFT));
   data[0] = 0x22;
   __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "write 0x22  %d", data[1]);
   rc2 = ioctl(fd, PMIC_WRITE, data);
   */

  // switch on
  data[1] = datareg;
  data[1] &= ~(V3GTX_EN_MASK << (V3GTX_EN_SHIFT));
  data[1] |= (1 << (V3GTX_EN_SHIFT));
  data[0] = 0x22;
  //__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "write 0x22  %d", data[1]);
  rc2 = ioctl(fd, PMIC_WRITE, data);

  close(fd);

  return;
}

void
Java_com_steuerung_heizung_SteuerungService_pmicoff(JNIEnv* env, jobject thiz)
{
  int rc2 = 0, fd = 0;
  int i;

  uint32_t data[2];
  uint32_t datareg;

  char buf[10] =
    { 0 };

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "pmic");

  fd = open("/dev/MT6326-pmic", O_RDWR);
  if (fd < 0)
    {

      //__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG,
      //		"/dev/MT6326-pmic open fail %d", fd);

      return;
    }

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "/dev/MT6326-pmic open  %d",
      fd);

  datareg = 0x21;

  rc2 = ioctl(fd, PMIC_READ, &datareg);

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "read  %d", datareg);

  /*

   data[1] = datareg;
   data[1] &= ~(V3GTX_CAL_MASK << (V3GTX_CAL_SHIFT));
   data[1] |= (0 << (V3GTX_CAL_SHIFT));
   data[0] = 0x21;
   rc2 = ioctl(fd, PMIC_WRITE, data);

   // 2_8 Volt
   //data[1] = 0;
   data[1] &= ~(V3GTX_SEL_MASK << (V3GTX_SEL_SHIFT));
   data[1] |= (0 << (V3GTX_SEL_SHIFT));
   data[0] = 0x21;
   rc2 = ioctl(fd, PMIC_WRITE, data);
   */
  datareg = 0x22;

  rc2 = ioctl(fd, PMIC_READ, &datareg);

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "read 0x22  %d", datareg);

  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "pmic read  %d", data);

  /*
   data[1] = datareg;
   data[1] &= ~(V3GTX_ON_SEL_MASK << (V3GTX_ON_SEL_SHIFT));
   data[1] |= (1 << (V3GTX_ON_SEL_SHIFT));
   data[0] = 0x22;
   __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "write 0x22  %d", data[1]);
   rc2 = ioctl(fd, PMIC_WRITE, data);
   */

  // switch on
  data[1] = datareg;
  data[1] &= ~(V3GTX_EN_MASK << (V3GTX_EN_SHIFT));
  data[1] |= (0 << (V3GTX_EN_SHIFT));
  data[0] = 0x22;
  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "write 0x22  %d", data[1]);
  rc2 = ioctl(fd, PMIC_WRITE, data);

  close(fd);

  return;
}
