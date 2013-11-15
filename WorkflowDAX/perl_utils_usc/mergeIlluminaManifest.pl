#!/usr/bin/perl

use strict;

use Insitu::csv_utils;
use File::Basename qw/basename/;

my $LINK_FLDS = [qw/Target TargetID/];
my $MANIFEST_FLDS = [qw/Transcript Accession Symbol Probe_Sequence Definition Ontology Synonym/];

# # Differential file version
# my $ML_FLDS = ['AVG_Signal-Group 1', 'AVG_Signal-Group 2', 'ARRAY_STDEV-Group 1', 'ARRAY_STDEV-Group 2',
# 	       'NARRAYS-Group 1', 'NARRAYS-Group 2','Detection-Group 1','Detection-Group 2'];

# # Raw file version
# my $ML_FLDS = [qw/AVG_Signal-1718911127_A BEAD_STDEV-1718911127_A Detection-1718911127_A/,
# 	       qw/AVG_Signal-1718911127_C BEAD_STDEV-1718911127_C Detection-1718911127_C/,
# 	       qw/AVG_Signal-1718911127_D BEAD_STDEV-1718911127_D Detection-1718911127_D/,
# 	       qw/AVG_Signal-1718911127_B BEAD_STDEV-1718911127_B Detection-1718911127_B/,
# 	       qw/AVG_Signal-1718911127_E BEAD_STDEV-1718911127_E Detection-1718911127_E/,
# 	       qw/AVG_Signal-1718911127_F BEAD_STDEV-1718911127_F Detection-1718911127_F/];

# Friedman version.  Should probably automate this
my $ML_FLDS = [qw/AVG_Signal-1930630091_A BEAD_STDEV-1930630091_A Detection-1930630091_A/,
	       qw/AVG_Signal-1930630091_B BEAD_STDEV-1930630091_B Detection-1930630091_B/,
	       qw/AVG_Signal-1930630091_C BEAD_STDEV-1930630091_C Detection-1930630091_C/,
	       qw/AVG_Signal-1930630091_D BEAD_STDEV-1930630091_D Detection-1930630091_D/,
	       qw/AVG_Signal-1930630091_E BEAD_STDEV-1930630091_E Detection-1930630091_E/,
	       qw/AVG_Signal-1930630091_F BEAD_STDEV-1930630091_F Detection-1930630091_F/,
	       qw/AVG_Signal-1930630090_A BEAD_STDEV-1930630090_A Detection-1930630090_A/,
	       qw/AVG_Signal-1930630090_B BEAD_STDEV-1930630090_B Detection-1930630090_B/,
	       qw/AVG_Signal-1930630090_C BEAD_STDEV-1930630090_C Detection-1930630090_C/,
	       qw/AVG_Signal-1930630090_D BEAD_STDEV-1930630090_D Detection-1930630090_D/,
	       qw/AVG_Signal-1930630090_E BEAD_STDEV-1930630090_E Detection-1930630090_E/,
	       qw/AVG_Signal-1930630090_F BEAD_STDEV-1930630090_F Detection-1930630090_F/];
	    
# Constants
my $WINLINE = chr(0xd);

# Get the file info
my ($manifest_fn, $diff_fn) = @ARGV;
my $base = basename($diff_fn, (".txt", ".csv"));
print STDERR "base=$base\n";

# Collect info from the manifest
print STDERR "About to read $manifest_fn\n";
my $mans = Insitu::csv_utils::csv_file_to_hash_list($manifest_fn);
my $num_mans = scalar(@$mans);
print STDERR "Found $num_mans manifests\n";
my $man_flds_by_link = {};
foreach my $man (@$mans)
{
    my $link = $man->{@{$LINK_FLDS}[0]};
    #print STDERR "MAN LINK = $link\n";
    my $flds = [map { $a = $man->{$_}; $a =~ s/,//g; $a } @$MANIFEST_FLDS];
    $man_flds_by_link->{$link} = $flds;
    # print "Adding flds: ".join(", ",@$flds)."\n";
}

# Start some output files
my $diffout = "${base}-GENES.csv";
die "Can't write to $diffout\n" unless (open(DIFFOUT, ">$diffout"));
my $ml = "${base}-MATLAB.csv";
die "Can't write to $ml\n" unless (open(ML, ">$ml"));
my $names = "${base}-NAMES.csv";
die "Can't write to $names\n" unless (open(NAMES, ">$names"));
my $accs = "${base}-ACCESSIONS.csv";
die "Can't write to $accs\n" unless (open(ACCS, ">$accs"));
my $func = "${base}-FUNCTIONALINFO.csv";
die "Can't write to $func\n" unless (open(FUNC, ">$func"));

# Now go through Diffs
print STDERR "About to read $diff_fn\n";
my $diffs = Insitu::csv_utils::csv_file_to_hash_list($diff_fn,"\t");
my $num_diffs = scalar(@$diffs);
print STDERR "Found $num_diffs diffs\n";
my $diff_num = 1;
my @heads = ();
foreach my $diff (@$diffs)
{
    if ($diff_num == 1)
    {
	my $bh = $diff->{backhash};
	my $ind = 0;
	while (my $fld = $bh->{$ind++})
	{
	    push(@heads, $fld);
	}
	print STDERR "Heads: ".join(", ", @heads)."\n";
	print DIFFOUT join(",", @heads, @$MANIFEST_FLDS)."\n";
    }

    # Get linked info
    my $link = $diff->{@{$LINK_FLDS}[1]};
    my $linked_flds = $man_flds_by_link->{$link};
    if (!$linked_flds)
    {
	die "Why is there no manifest line for \"${link}?\n";
    }
    #print "Found flds: ".join(", ",@$linked_flds)."\n";

    print DIFFOUT join(",", 
		       (map {$diff->{$_}} @heads),
		       @$linked_flds). "\n";
    print ML join(",", map {$diff->{$_}} @$ML_FLDS) . "\n";
    
    # And get the name
    my $name = @{$linked_flds}[2];
    $name = @{$linked_flds}[1] unless ($name);
    $name = @{$linked_flds}[0] unless ($name);
    print NAMES "${name}\n";

    my $acc = @{$linked_flds}[1];
    print ACCS $acc."\n";


#    print STDERR "T=$target\tG1=$g1avg\tG2=$g2avg\tD=$diff_score\n";

    $diff_num++;
}

close(DIFFOUT);
close(ML);
close(NAMES);
close(ACCS);
close(FUNC);
