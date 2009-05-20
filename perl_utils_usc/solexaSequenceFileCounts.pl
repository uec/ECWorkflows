#!/usr/bin/perl

use strict;
use File::Basename qw/basename/;



my @files = @ARGV;

my $ADAPTER_PATTERN = '^GATCGGAAG';
my $POLYA_PATTERN = '^AAAAA+[ACTGN]AAAAA+[ACTGN]AAAAA+[ACTGN]AAAAA+[ACTGN]?$';

my $file_num = 0;
foreach my $f (@files)
{
#   Parse the filename
    my @dirs = split(/\//,$f);
    my $n_dirs = scalar(@dirs);
    die "Can't parse $f\n" unless ($n_dirs >= 6);
    my ($exp, $data, $firecrest, $bustard, $gerald, $seq_file) = @dirs[$n_dirs-6 ..  $n_dirs-1];
    my $prefix = ($n_dirs>6) ? join("\/",@dirs[0 .. $n_dirs-7]) : "";
    die "Can't parse sequence file $seq_file\n" unless ($seq_file =~ /s_(\d+)_sequence/);
    my $lane = $1;

    my $desc = "${exp}_${gerald}_${lane}";
    print STDERR "$desc\n";

    my $counts = [];

    # Now count the total number of clusters
    my $cluster_file = $prefix . "\/" . join("\/",$exp,$data,$firecrest,$bustard,"s_${lane}_0100_seq.txt");
    my $cluster_files = $prefix . "\/" . join("\/",$exp,$data,$firecrest,$bustard,"s_${lane}_*_seq.txt");
    
die "Can't read $cluster_file\n" unless (-f $cluster_file);
    my $cmd = "cat $cluster_files | wc -l";
    #print STDERR "$cmd\n";
    my $n_clust = `$cmd`;
    chomp $n_clust;

    # Count the number of aligned clusters
    my $eland_file = $prefix . "\/" . join("\/",$exp,$data,$firecrest,$bustard,$gerald,"s_${lane}_eland_result.txt");
    my $n_uniquely_aligned = -1;
    if (-f $eland_file)
    {
	#print STDERR "Eland file exists\n";
	my $cmd = "awk '(\$3 ~ \/\^U\/) {print \$3}' $eland_file | wc -l";
	print STDERR "$cmd\n";
	$n_uniquely_aligned = `$cmd`;
	chomp $n_uniquely_aligned;
    }

    # Go through PF seq file
    my $n_pf = 0;
    my $n_polya = 0;
    my $n_adapt = 0;
    die "Can't read $f\n" unless (open(F,$f));
    while (my $line = <F>)
    {
	if ($line =~ /^[GCATN][GCATN][GCATN]/)
	{
	    $n_pf++;

	    if ($line =~ /$ADAPTER_PATTERN/)
	    {
		$n_adapt++;
	    }
	    elsif ($line =~ /$POLYA_PATTERN/)
	    {
		$n_polya++;
	    }
	}
    }
    close(F);

    if ($file_num++ == 0)
    {
	print join("\t", qw/Exp lane clusts_total clusts_pf adapters polya uniquely_aligned/);
	print "\n";
    }

    print join("\t", $desc, $lane,
	       $n_clust, $n_pf, $n_adapt, $n_polya, $n_uniquely_aligned);
    print "\n";

}
