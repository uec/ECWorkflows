#!/usr/bin/perl
use strict;

my $USAGE = "maqDirectorySummary.pl basename";

die "$USAGE\n" if (@ARGV < 1);

my ($basename) = @ARGV;


my $stats = {};


my $ext = (-f "${basename}contam.fastq") ? "fastq" : "txt";


my $contam_seqs = "${basename}contam.${ext}";
die "Can't find $contam_seqs\n" unless (-f $contam_seqs);
my $adapter_exact = `wc -l $contam_seqs` / 4;
$stats->{"4-Adapter-10bp-exact"} += $adapter_exact;

my $nocontam_seqs = "${basename}nocontam.${ext}";
die "Can't find $nocontam_seqs\n" unless (-f $nocontam_seqs);
my $noadapter = `wc -l $nocontam_seqs` / 4;
$stats->{"0-Nocontam"} += $noadapter;

my $total = $adapter_exact + $noadapter;
$stats->{"0-Total"} += $total;


my $map_fn = "${basename}nocontam.map.txt";
die "Can't open $map_fn\n" unless (open(F,$map_fn));

my $mappable = 0;
  LINE: while (my $line = <F>)
  {
      $mappable++;

      chomp $line;
      my @flds = split(/\t/,$line);

      my $seq_name = @flds[0];
      my $locus = @flds[1];
      my $pos_raw = @flds[2];
      my $strand = @flds[3];
      my $seq = @flds[14];
      my $q = @flds[7];
      my $num_0m = @flds[11];
      my $num_1m = @flds[12];
      my $uniq = (($num_0m<=1) && ($num_1m<=1));
	  

      if ($locus =~ /contam/i)
      {
	  $stats->{"4-Adapter-Maq"}++;
      }
      else
      {
	  foreach my $q_range ([0,10],[10,20],[20,30],[30,40],[40,50],[50,10000000])
	  {
	      my ($minQ,$maxQ) = @$q_range;

	      if (($q >= $minQ) && ($q < $maxQ))
	      {
		  my $s_name = ($uniq) ? "1-unique" : "2-multi";
		  $s_name .= "-Q${minQ}-Q${maxQ}";
		  $stats->{$s_name}++;
	      }

	  }
      }

  }
close(F);

my $unmappable = $noadapter - $mappable;
$stats->{"3-Unmappable"} += $unmappable;



foreach my $s (sort(keys(%$stats)))
{
    my $v = $stats->{$s};

    my $frac = ($v/$total) * 100;

    print "$s\t$v\t$frac\n";
}
