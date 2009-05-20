#!/usr/bin/perl


use strict;
use File::Basename qw/basename/;

my $USAGE = "differentialGenesVsChIPSites.pl differential_genes.txt tss.gff chip_sites.gff boundary_sites.gff";

die "$USAGE\n" if (@ARGV<3 || @ARGV>4);

my ($diff_txt_fn, $tss_gff_fn, $chip_gff_fn, $boundary_gff_fn) = @ARGV;


# Build a basename from diff file, and make a directory
my $base = $diff_txt_fn;
$base =~ s/\.\w+$//g;
print STDERR "Base: $base\n";
my $dir = "${base}-processed";
my $cmd = "rm -rf $dir; mkdir $dir";
print STDERR "$cmd\n";
`$cmd`;

# First get the differential genes
die "Can't read $diff_txt_fn\n" unless (open(F,$diff_txt_fn));
my $diffs = {};
while (my $line = <F>)
{
    chomp $line;
#    print STDERR "line: \"$line\"\n";
    $diffs->{$line}++;
}
close(F);

# Now generate the TSS file
my $diff_tss_fn = $dir."/tss.gff";
die "Can't write to $diff_tss_fn\n" unless (open(DIFFTSS,">$diff_tss_fn"));
die "Can't read $tss_gff_fn\n" unless (open(TSS, $tss_gff_fn));
while (my $line = <TSS>)
{
    next unless ($line =~ /gene_id\s+\"(?:[^\"]+\-\-)?([^\.\"]+)(\.[^\"]+)?\";/);
    my $gid = $1;
    my $tx = $2;
    if ($diffs->{"${gid}${tx}"} || $diffs->{$gid})
    {
	print STDERR "Found g:$gid tx:$tx\n";
	print DIFFTSS $line;
    } 
}
close(TSS);
close(DIFFTSS);

# Dist from chip sites
my $chip_dist_fn = $dir."/dist_from_ChIP.csv";
my $cmd = "java NearestFeatures $chip_gff_fn $diff_tss_fn > $chip_dist_fn";
print STDERR "$cmd\n";
`$cmd`;






