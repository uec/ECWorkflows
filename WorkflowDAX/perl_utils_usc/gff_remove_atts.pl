#!/usr/bin/perl

use strict;
use File::Basename;
use File::Temp qw/:mktemp/;

my @files = @ARGV;


foreach my $f (@files)
{
    print STDERR "Working on $f\n";
    die "Couldn't read $f\n" unless (open(FIN,$f));
    my ($fh_out, $f_out) = mkstemp( "tempGffFileXXXXX" );

    while (my $line = <FIN>)
    {
	chomp $line;
	my @flds = split(/\t/, $line);
	
	if (@flds >= 8)
	{
	    $line = join("\t",@flds[0..7]);
	}
    
	print $fh_out "${line}\n";
    }

    close($fh_out);
    close(FIN);

    `cp $f ${f}.OLD`;
    `mv $f_out $f`;
}
