all:
	@echo `date`
src=/Application/monkey
#state=/Application/etl/state
#jar=/Application/etl/jar
#conf=/Application/etl/conf
#fresh:
#	rm -rf Makefile
clean:
	rm -rf ${src}
	rm -f /usr/bin/monkey
	rm -f /usr/bin/updown
	@echo "clean ok"

install:
	rm -rf ${src}
	rm -f /usr/bin/monkey
	rm -f /usr/bin/updown
	mkdir -p -m 777 ${src}
	cp ./* -r ${src}
#	cp ./conf/monkey /usr/bin
	ln -s ${src}/conf/updown /usr/bin/updown 
	ln -s ${src}/conf/monkey /usr/bin/monkey
#	rm -rf ${src}/monkey-1.spec
	chmod -R 777 ${src}
uninstall:
	rm -rf ${src}
	rm -f /usr/bin/monkey
	rm -f /usr/bin/updown

