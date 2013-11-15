#!/usr/bin/perl

use strict;
use File::Basename qw/basename fileparse/;

my @files = @ARGV;

foreach my $f (@files)
{
    die "Can't open $f\n" unless (open(F,$f));

    my $fbase = fileparse($f,qr/\.[^\.]+/);
    my $outf = "${fbase}.bed";
    print STDERR "Out: $outf\n";
    die "Can't write to $outf\n" unless (open(OUTF,">$outf"));


#    print OUTF "track type=wiggle_0 name=$fbase description=$fbase visibility=2 maxHeightPixels=128:25:11 graphType=bar windowingFunction=mean autoScale=off\n";
    print OUTF "track name=$fbase description=$fbase visibility=2\n";

    my $last_c = 0;
    my $last_s = -1;
    my $last_e = -1;
    my $misorders_seen = 0;
    while (my $line = <F>)
    {

	next if ($line =~ /track/);
	chomp $line;

	my @flds = split(/\t/,$line);
	print STDERR $line unless (@flds >= 5);

	my $c = @flds[0];
	my $s = @flds[3];
	my $e = @flds[4];
	my $score = @flds[5];

	$score = 50 if ($score eq '.'); # Illegal in bed


	if ( ($s >= $last_s) && ($s <= $last_e))
	{
	    # Wig files can't have this either.
	    print STDERR "Start of next ($s) is less than end of prev ($last_e). fixing\n";
	    $s = $last_e + 1;
	}

	    my $out = join("\t",$c,$s,$e,$score);
	    print OUTF $out."\n";




	    $last_e = $e;
	    $last_c = $c;
	    $last_s = $s;


# 	if (($c eq $last_c) && ($s < $last_s))
# 	{
# 	    $misorders_seen++;
# 	    print STDERR "Misordered line \#$misorders_seen:\n$line\n";
# 	}
# 	else
# 	{
# 	}

    }

    close (OUTF);
    close(F);
}
