#!/usr/bin/perl

use strict;

my ($minq, $min_reads) = @ARGV;
die "USAGE: maqReport.pl MIN_Q [MIN_READS] \n" unless ($minq);


my $counts = {};
my $chrom_counts = {};
my $order = {};

my $total_aligned = 0;
my $total_CGs = 0;
my $total_contam = 0;
my $line_n = 1;
LINE: while (my $line = <STDIN>)
{
    chomp $line;
    my @flds = split(/\t/,$line);

    my $locus = @flds[1];
    my $seq = @flds[14];
    my $q = @flds[7];
    my $c_or_t = lc(substr($seq,0,1));
#    print "$locus\t$c_or_t\n";

    $locus =~ /(chr[0-9XY]+)/;
    my $chr = $1;
    if (!$chr)
    {
#	print STDERR "Bad chr: $locus\n";
	$total_contam++;
	next LINE;
    }

    next LINE unless ($q >= $minq);
    $total_aligned++;
    
    if (($c_or_t ne 't') && ($c_or_t ne 'c'))
    {
#	print STDERR "not CGG:\t$seq\n";
    }
    else
    {

	$total_CGs++;

	$counts->{$locus} = [] unless ($counts->{$locus});
	$chrom_counts->{$chr} = [] unless ($chrom_counts->{$chr});
	
	if ($c_or_t eq 't')
	{
	    @{$counts->{$locus}}[0]++;
	    @{$chrom_counts->{$chr}}[0]++;
	}
	else
	{
	    @{$counts->{$locus}}[1]++;
	    @{$chrom_counts->{$chr}}[1]++;
	}
	$order->{$locus} = $line_n++ unless ($order->{$locus});
	$order->{$chr} = $line_n++ unless ($order->{$chr});
    }

}


print "Total Q>=${minq}:\t$total_aligned\n";
print "Total CGs:\t$total_CGs\n";
print "Min contam:\t$total_contam\n";

print "\n\n";

my $loci = {};
foreach my $locus (sort {$order->{$a} <=> $order->{$b}} keys(%$counts))
{
    my ($unmeth,$meth) = @{$counts->{$locus}};
    my $frac = $meth / ($unmeth + $meth);
#    printf("%s\t%d\t%d\t%0.2f\n",$locus,$unmeth,$meth,$frac);

    # By chromosome
    $locus =~ /(chr[0-9XY]+)_/;
    my $chr = $1;

    $loci->{$chr} = [] unless ($loci->{$chr});

    if (($unmeth+$meth)>=$min_reads)
    {
    if ($frac < 0.25)
    {
	@{$loci->{$chr}}[0]++;
    }
    elsif ($frac > 0.75)
    {
	@{$loci->{$chr}}[2]++;
    }
    else
    {
	@{$loci->{$chr}}[1]++;
    }
}
}

print "\n\n";

foreach my $chr (sort {$order->{$a} <=> $order->{$b}} keys(%$chrom_counts))
{

    my ($l,$m,$h) = @{$loci->{$chr}};
    my $total = $l+$m+$h;

    printf("%s\t%d\t%d\t%d\t%d\t%0.2f\t%0.2f\t%0.2f\n",
	   $chr,
	   $total, 
	   $l, $m, $h,
	   $l/$total, $m/$total, $h/$total);
}
