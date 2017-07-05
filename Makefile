VERSION := 0.3.0
RELEASEPATH = release/$(FULLVERSION)

UNAME := $(shell uname)
ifeq ($(UNAME), Darwin)
	SED=sed -i ""
else
	SED=sed -i
endif

default:
	mkdir -p release/hangout-$(VERSION)
	mvn clean package -DskipTests
	unzip hangout-dist/target/hangout-dist-0.3.0-release-bin.zip -d release/hangout-$(VERSION)
	git rev-parse --short HEAD > release/hangout-$(VERSION)/VERSION
	chmod +x release/hangout-$(VERSION)/bin/hangout

clean:
	rm -rf release
	mvn clean
