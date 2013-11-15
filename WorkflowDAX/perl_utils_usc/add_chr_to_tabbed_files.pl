#!/usr/bin/perl

use strict;

use File::Temp qw/tempfile/;

foreach my $fin (@ARGV)
{
    if ($fin =~ /(chr\d+)/i)
    {
	my $chr = lc($1);
	my ($tempfh, $tempfn) = tempfile();

	die "Can't read $fin" unless (open(IN, $fin));

	while (my $line = <IN>)
	{
	    my $out = $line;
	    if ($line =~ /^\d/)
	    {
		$out = "$chr\t$line";
	    }
	    print $tempfh $out;
	}

	close($tempfh);
	close(IN);
	`mv $fin ${fin}.ORIG`;
	`mv $tempfn $fin`;
    }
}
