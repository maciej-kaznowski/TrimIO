<img src="https://raw.githubusercontent.com/maciej-kaznowski/TrimIO/master/googleplay/ic_launcher.png" width="100">

# TrimIO

<img src="https://raw.githubusercontent.com/maciej-kaznowski/TrimIO/master/docs/img/Screenshot_TrimIO_20190806-235637.png" width="200">
<img src="https://raw.githubusercontent.com/maciej-kaznowski/TrimIO/master/docs/img/Screenshot_TrimIO_20190806-235707.png" width="200">
<img src="https://raw.githubusercontent.com/maciej-kaznowski/TrimIO/master/docs/img/Screenshot_TrimIO_20190806-235647.png" width="200">

This is a simple Android app to trim the NAND storage. It uses the linux command `fstrim`.  
Whilst modern Android phones trim the storage automatically, you can't invoke this process manually. Trimming manually is recommended after deleting large amounts of data, and can increase IO performance.  

<b>To read about trimming, see the following:</b>
* https://en.wikipedia.org/wiki/Trim_(computing)
* http://man7.org/linux/man-pages/man8/fstrim.8.html