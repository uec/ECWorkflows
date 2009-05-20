#!/usr/bin/perl

use strict;
use File::Basename qw/basename/;

# CONSTANTS
my $USAGE = "agilentSplitGtfSources.pl clusts.gtf";

my ($fn) = @ARGV;
my $base = basename($fn,('.gtf','.gff'));
print STDERR "base=$base\n";

my $info_fn = "${base}-INFO.csv";
my $map_fn = "${base}-MAP.csv";
my $names_fn = "${base}-NAMES.csv";

die "Can't read $fn\n" unless (open(IN,$fn));
die "Can't write to $info_fn\n" unless (open(INFO,">$info_fn"));
die "Can't write to $names_fn\n" unless (open(NAMES,">$names_fn"));

my @map_l = ();
my $on_feat = 0;
while (my $line = <IN>)
{
    chomp($line);
    my ($chr, $feat_l, $kind, $s, $e, $score, $strand, $frame, $atts) = split(/\t/,$line);

    next unless ($chr =~ /chr/i);

    $on_feat++;
    $chr =~ s/chr//gi;
    $chr =~ s/X/23/gi;
    $chr =~ s/Y/24/gi;

    my @feats = split(/,/,$feat_l);
    map { $map_l[$_-1] = $on_feat  } @feats;

    $atts =~ /gene_id\s+\"([^\"]*)\"/;
    my $n = $1;
    $n =~ s/\,//g;

    print INFO join(",",$on_feat,$chr,$s,$e)."\n";
    print NAMES "$n\n";
}


close(IN);
close(INFO);
close(NAMES);


die "Can't write to $map_fn\n" unless (open(MAP,">$map_fn"));
for (my $i = 0; $i < scalar(@map_l); $i++)
{
    print MAP $map_l[$i]."\n";
}
close(MAP);

