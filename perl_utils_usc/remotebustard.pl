#!/usr/bin/perl
use strict;


my $username;
my $remoteDir;
my $flowcell;

#USER NAME
for my $s (@ARGV)
{
	if($s =~ /remoteuser=(\S+)/)
	{
		$username = $1;
	}
}
#Remote TMP
if($ARGV[$i] =~ /remotetmpdir=(\S+)/)
{
	$remoteDir = $1;
}

#FlowCell
if($ARGV[$i] =~ /flowcell=(\S+)/)
{
	$flowcell = $1;
}
my @hosts = qw(gastorage2);

for my $host (@hosts)
{
	print("ssh hpc-login2 \"ssh $username\@$host nohup /srv/software/ECWorkflow/bustard/bustard.pl " . join(" ", @ARGV) . "\"");
	system("ssh hpc-login2 \"ssh $username\@$host nohup /srv/software/ECWorkflow/bustard/bustard.pl " . join(" ", @ARGV) . "\"");
}

while(!(-e "$remoteDir/$flowcell/ready_for_hpcc.done"))
{
	sleep 60;
}
