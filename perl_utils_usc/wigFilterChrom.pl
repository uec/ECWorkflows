#!/usr/bin/perl

use strict;

my $USAGE = "wigFilterChrom.pl chr1 [start end]";

die "$USAGE\n" if ((@ARGV < 1) || (@ARGV > 3));

my $target_chr = @ARGV[0];
my $target_s = @ARGV[1];
my $target_e = @ARGV[2];

LINE: while (my $line = <STDIN>)
{
    chomp $line;
    if ($line =~ /^(chr\S+)/i)
    {
	my $chr = $1;
	next LINE unless (uc($chr) eq uc($target_chr));
	
	my ($chr,$s,$e,$count) = split(/\t/,$line);
	next LINE if ($target_s && ($e < $target_s));
	next LINE if ($target_e && ($s > $target_e));

	print $line."\n";
    }
    else
    {
	print $line."\n";
    }
}

