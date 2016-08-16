VERSION := 0.1.8
RELEASEPATH = release/$(FULLVERSION)

default: 2.3.2

all: 2.3.2 2.3.5

clean:
	rm -rf release

build:
	git checkout $(GITBRANCH)
	mkdir -p $(RELEASEPATH)
	mkdir -p $(RELEASEPATH)/bin
	mkdir -p $(RELEASEPATH)/lib
	mkdir -p $(RELEASEPATH)/vender
	cp example.yml $(RELEASEPATH)
	cp LICENSE $(RELEASEPATH)
	cp bin/hangout $(RELEASEPATH)/bin
	sed -i "" 's/\<elasticsearch-version\>[0-9.]*/<elasticsearch-version\>$(ESVERSION)/' pom.xml
	git rev-parse --short HEAD > $(RELEASEPATH)/VERSION
	mvn clean package
	mvn dependency:copy-dependencies
	cp target/hangout-$(VERSION).jar $(RELEASEPATH)/lib
	cp target/dependency/* $(RELEASEPATH)/vender
	sed -i '' 's/X.X.X/$(VERSION)/' $(RELEASEPATH)/bin/hangout
	tar -cf release/$(FULLVERSION).tar -C release $(FULLVERSION)
	git reset --hard

test:
	$(MAKE) build FULLVERSION=hangout-test GITBRANCH=$@ ESVERSION=2.3.5

dev:
	$(MAKE) build FULLVERSION=hangout-dev GITBRANCH=$@ ESVERSION=2.3.5

2.3.2:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=master ESVERSION=2.3.2

2.3.5:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=master ESVERSION=2.3.5
