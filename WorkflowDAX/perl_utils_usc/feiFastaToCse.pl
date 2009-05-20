#!/usr/bin/perl

use strict;
use File::Basename qw/basename fileparse/;

my @files = @ARGV;

foreach my $f (@files)
{
    die "Can't open $f\n" unless (open(F,$f));

    my $fbase = fileparse($f,qr/\.[^\.]+/);
    my $outf = "${fbase}.cse";
    print STDERR "Out: $outf\n";
    die "Can't write to $outf\n" unless (open(OUTF,">$outf"));


    my $seen = {};
    while (my $line = <F>)
    {
	if ($line =~ /^>/)
	{
	    die "Found illegal line:\n$line" unless ($line =~ /chr([^_]+).*start\:(\d+).*end\:(\d+).*strand\:([FB]W)/i);

	    # They are sometimes backwards
	    my $s = ($3 > $2) ? $2 : $3;
	    my $e = ($3 > $2) ? $3 : $2;

	    my $chr = $1;
	    $chr =~ s/X/23/ig;
	    $chr =~ s/Y/24/ig;

	    my $out = join(",",$chr,$s,$e);
#	    print STDERR $out."\n" if ($line =~ /chrX/);
	    print OUTF $out."\n" unless ($seen->{$out});

	    $seen->{$out}++;
	}
    }

    close (OUTF);
    close(F);
}
