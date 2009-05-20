#!/usr/bin/perl

use strict;
use File::Basename qw/basename/;

my $USAGE = "solexaTileErrorStats.pl ./Solexa.../Data/C1-36.../Bustard.../GERALD.../s_1_*_score.pl";

my @files = @ARGV;

my $file_num = 0;
FILE: foreach my $f (@files)
{
    $file_num++;

#   Parse the filename
    my @dirs = split(/\//,$f);
    my $n_dirs = scalar(@dirs);
    die "Can't parse $f\n" unless ($n_dirs >= 6);
    my ($exp, $data, $firecrest, $bustard, $gerald, $seq_file) = @dirs[$n_dirs-6 ..  $n_dirs-1];
    my $prefix = ($n_dirs>6) ? join("\/",@dirs[0 .. $n_dirs-7]) : "";
    die "Can't parse sequence file $seq_file\n" unless ($seq_file =~ /s_(\d+)_(\d+)_/);
    my $lane = $1;
    my $tile = $2;
    my $desc = "${exp}_${gerald}_${lane}";

    die "Can't find date:\n$f\n" unless ($f =~ /(\d+)\-(\d+)\-20(\d+)/);
    my ($d,$m,$y) = ($1,$2,$3);
    my $date = "$y$m$d";

    if (($tile < 101) || ($tile > 200))
    {
	# print STDERR "Skipping tile $tile (only use 101-200)\n";
	next FILE;
    }

    # Get cycle errors
    my @cycle_errors = cycleErrors($f);

    # get total number of seqs in the tile
    my $seq_file = $prefix . "\/" . join("\/",$exp,$data,$firecrest,$bustard,"s_${lane}_${tile}_seq.txt");
    my $n_clust = lineCountFile($seq_file);

    print join(",", $date, $file_num, $lane, $tile, $n_clust, @cycle_errors);
    print "\n";

}


sub lineCountFile
{
    my ($f) = @_;

    die "Can't open $f\n" unless open(F,$f);
    my $count=0; 
    LINES: while (my $line = <F>)
    {
	$count++;
    }
    return $count;
}


sub cycleErrors
{
    my ($f) = @_;

    die "Can't open $f\n" unless open(F,$f);

    my $in_cycle = 0;
    my @cycle_errors = 0;
    my $tile_bases = 0;
    LINES: while (my $line = <F>)
    {
	chomp $line;
	if ($in_cycle)
	{
	    # End of cycle
	    if ($line =~ /^\s*$/)
	    {
		$in_cycle = 0;
		last LINES;
	    }
	    else
	    {
		my ($cycle, $err, $blank) = split(/\t/,$line);
		
		$cycle_errors[$cycle-1] = $err + $blank;
	    }
	}
	else
	{
	    if ($line =~ /^Cycle\:/)
	    {
		$in_cycle = 1;
	    }
	    elsif ($line =~ /(\d+)\s+bases\s+of\s+sequence/)
	    {
		$tile_bases = $1;
	    }
	}

    }
    close(F);

    my $n_cycles = scalar(@cycle_errors);
    my $n_clusts = $tile_bases / $n_cycles;

    # print STDERR "$f\t$n_cycles\t$n_clusts\n";
    
    return ($n_clusts,@cycle_errors);
}
