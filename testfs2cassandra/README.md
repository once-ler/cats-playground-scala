### TikaOCR
Reference [https://cwiki.apache.org/confluence/display/tika/TikaOCR](https://cwiki.apache.org/confluence/display/tika/TikaOCR) 

#### Installing Tesseract on RHEL
1. Add "epel" to your yum repositories if it isn't already installed 
1a. wget https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm (or appropriate version) 
1b. rpm -Uvh epel-release-latest-7.noarch.rpm 
2. yum install tesseract 
3. To add language packs, see what's available yum search tesseract then, e.g. yum install tesseract-langpack-ara 

#### Installing Tesseract on Ubuntu
1. sudo apt-get update 
2. sudo apt-get install tesseract-ocr 
3. To add language packs, see what's available then, e.g. sudo apt-get install tesseract-ocr-fra

#### Mac Installation Instructions
1. If you are lucky brew install tesseract --with-all-languages --with-serial-num-pack will work, if not, read on 

#### Issues with Installing via Brew
If you have trouble installing via Brew, some options to try:
1. try typing brew -v install tesseract --with-all-languages --with-serial-num-pack 
2. try to discern any make/configure errors. YMMV here. 
3. if brew won't do it, you can also try and install Tesseract from source. 

#### Tesseract won't work with TIFF files
If you are having trouble getting Tesseract to work with TIFF files, read this link. Summary:
     
1. uninstall tesseract brew uninstall tesseract 
2. uninstall leptonica brew uninstall leptonica 
3. install leptonica with tiff support brew install leptonica --with-libtiff 
4. install tesseract brew install tesseract --with-all-languages --with-serial-num-pack
