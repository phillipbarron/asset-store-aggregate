%define name asset-store-aggregate
Name: %{name}
Version: %{?buildnum}%{!?buildnum:0}
Release: 1%{?dist}

%define buildroot %(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)

Summary: oasset-store-aggregate

Group: Installation Script
License: internal BBC use only
Source: %{name}.tar.gz
BuildRoot: %{buildroot}
Requires: cloud-httpd24-ssl-services-devs
Requires: java-1.8.0-openjdk
Requires: ibl-sysadmin
Requires: cps-collectd
Requires: cps-filebeat
Requires: jq
AutoReqProv: no

%description


%prep
%setup -q -c -n %{name}

%build

%pre
getent group asset-store-aggregate >/dev/null || groupadd -r asset-store-aggregate
getent passwd asset-store-aggregate >/dev/null || useradd -r -g asset-store-aggregate -G asset-store-aggregate -d / -s /sbin/nologin -c "asset-store-aggregate" asset-store-aggregate

%install
mkdir -p %{buildroot}/usr/lib/asset-store-aggregate
cp -r ./ %{buildroot}/usr/lib/asset-store-aggregate
mkdir -p %{buildroot}/var/log/asset-store-aggregate
mkdir -p %{buildroot}/etc/bake-scripts/asset-store-aggregate/
mv %{buildroot}/usr/lib/asset-store-aggregate/bake-scripts/* %{buildroot}/etc/bake-scripts/asset-store-aggregate/.
rm -fr %{buildroot}/usr/lib/asset-store-aggregate/bake-scripts

%post
systemctl enable /usr/lib/asset-store-aggregate/asset-store-aggregate.service

%clean
rm -rf %{buildroot}

%files
%defattr(644, asset-store-aggregate, asset-store-aggregate, 755)
/usr/lib/asset-store-aggregate
/var/log/asset-store-aggregate
%attr(755, root, root) /etc/bake-scripts/asset-store-aggregate/*
%attr(755, asset-store-aggregate, asset-store-aggregate) /usr/lib/asset-store-aggregate/target/scala-2.11/asset-store-aggregate.jar
