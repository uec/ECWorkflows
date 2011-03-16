#!/usr/bin/env perl

use strict;
use File::Find;
use File::Temp qw/tempfile/;
use Cwd;

my $USAGE = "solexaTraverseSeqfiles.pl dir1 dir2 .. ";

$::FOLLOW = 0;

die "$USAGE\n" unless (@ARGV >= 1);
my (@dirs) = @ARGV;


$::total = 0;
find({ wanted => \&wanted, follow => $::FOLLOW }, @dirs);
#print STDERR join(", ", @::map_files)."\n";
print STDERR "Total: $::total\n";

sub wanted
{
    my $file = $_;
    if (($file =~ /sequence.txt/) && (-s $file))
    {
	my $a = `wc -l $file`;

	die "wc -l $file\nReturned illegal output: $a\n"
	    unless ($a =~ /^(\d+)/);
	
	my $size = $1 / 4;
	print STDERR "$size\t$file\n";
	$::total+=$size;
    }
}

