Summary: the First RPM of sam
Name:monkey
Version:2.0.1
Release:1
Vendor:Fast sky (fast_sky@sina.com)
License:Share
Group:Applications/Text
Source0:monkey-1.tar.gz
#BuildRoot:%{_tmppath}/%{name}-%{version}-%{release}-root
#Patch0:etl-1.5.patch
%description
Etl 1.6
%prep
export RPM_SOURCES_DIR=/usr/src/redhat/SOURCES
export RPM_BUILD_DIR=/usr/src/redhat/BUILD
tar -xvf $RPM_SOURCES_DIR/monkey-1.tar.gz
#%patch
%build
cd $RPM_BUILD_DIR/monkey-1
make
%install
cd $RPM_BUILD_DIR/monkey-1
make install
%clean
rm -rf $RPM_BUILD_DIR/monkey-1
%files
%defattr(-,root,root)
%{_bindir}/monkey
#/usr/bin/etl
/Application/monkey
#%doc
#/Application/etl/readme
#/usr/src/redhat/BUILD/etl-1.5/readme
%changelog
* Tue Sep 21 2013 Fast sky (fast_sky@sina.com)
- Fast sky test it
