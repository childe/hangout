VERSION := 0.1.4
RELEASEPATH = release/$(FULLVERSION)

default: 2.2

all: 2.2 2.1 2.0 1.7 1.6 1.5 fws-hermes uat-hermes prod-hermes

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
	git rev-parse --short HEAD > $(RELEASEPATH)/VERSION
	mvn clean package
	mvn dependency:copy-dependencies
	cp target/hangout-$(VERSION).jar $(RELEASEPATH)/lib
	cp target/dependency/* $(RELEASEPATH)/vender
	sed -i '' 's/X.X.X/$(VERSION)/' $(RELEASEPATH)/bin/hangout
	tar -cf release/$(FULLVERSION).tar -C release $(FULLVERSION)

test:
	$(MAKE) build FULLVERSION=hangout-test GITBRANCH=$@

dev:
	$(MAKE) build FULLVERSION=hangout-dev GITBRANCH=$@

2.2:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=$@

2.1:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=$@

2.0:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=$@

1.7:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=$@

1.6:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=$@

1.5:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=$@
