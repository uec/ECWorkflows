#!/usr/bin/perl
#
# add_scores_to_gff.pl features.gff scores.csv match_id_field score_field1 score_field2
#
# csv file must have a header line as the first line. File names are determined by the
# headers for the score fields.
#
# GFF features must have an ID field (field #9) with IDs matching field match_id_field
# of the scores.csv file.

use lib "/Users/benb/research/cvs/insitu_flyboy/perl";

use strict;
use Insitu::csv_utils;
use File::Basename;

my $USAGE = "add_scores_to_gff.pl features.gff scores.csv default_score match_id_field score_field1 score_field2";

my ($gff, $csv, $default_score, $csv_id_fld, @csv_score_flds) = @ARGV;
die "$USAGE\n" unless (@ARGV >= 4);


# Go through and parse gff file.  We can't do a hash because
# sometimes the same ID has multiple GFF lines.
my $gff_entries = [];
die "Couldn't open $gff\n" unless (open(GFF,$gff));
GFF_LINE: while (my $line = <GFF>)
{
    chomp $line;
    next GFF_LINE if ($line =~ /^\s*\#/);
    next GFF_LINE if ($line =~ /^\s*$/);

    my @flds = split(/\t/,$line);
    next GFF_LINE unless (@flds[3] =~ /^\d+$/); # Maybe a header
    push(@$gff_entries, \@flds);
}
close(GFF);
print "Found ".scalar(@$gff_entries)." in $gff\n";

# my $id = "NM_001005221";
# print "Entries for $id:\n";
# map {print entry_line($_)} select_gff_entries($gff_entries,$id);

my ($csv_headers, $csv_entries) = Insitu::csv_utils::csv_file_to_hash_list($csv);
my $num_scores = scalar(@$csv_entries);
print "Found ".$num_scores." scores\n";

my $id_fld_name = @{$csv_headers}[$csv_id_fld-1];
print "ID field = \"$id_fld_name\"\n";

# Go through each score field
foreach my $csv_score_fld (@csv_score_flds)
{
    my $score_fld_name = @{$csv_headers}[$csv_score_fld-1];
    my $csv_scores_by_id = {};
    map
    {   
	my $id = $_->{$id_fld_name};
	my $score = $_->{$score_fld_name};
	$csv_scores_by_id->{uc($id)} = $score;
    } (@$csv_entries);

    my $out_fn = "./".basename($gff,qw/.gff .gtf/)."-${score_fld_name}.gtf";
    print "Out: $out_fn\n";
    die "Couldn't write to $out_fn\n" unless (open(ALL, ">$out_fn"));

#     my $out_fn_up = "./".basename($gff,qw/.gff .gtf/)."-${score_fld_name}-UP.gtf";
#     print "Out-up: $out_fn_up\n";
#     die "Couldn't write to $out_fn_up\n" unless (open(UP, ">$out_fn_up"));

#     my $out_fn_down = "./".basename($gff,qw/.gff .gtf/)."-${score_fld_name}-DOWN.gtf";
#     print "Out-down: $out_fn_down\n";
#     die "Couldn't write to $out_fn_down\n" unless (open(DOWN, ">$out_fn_down"));

    foreach my $gff_entry (@$gff_entries)
    {
	my $id = @{$gff_entry}[8];
	my $score = $csv_scores_by_id->{uc($id)};
	$score = $default_score unless (defined($score));
	@{$gff_entry}[5] = sprintf("%0.2e",$score);
	print ALL entry_line($gff_entry);
    }
    
#     foreach my $csv_entry (@{$csv_entries})
#     {
# 	my $id = $csv_entry->{$id_fld_name};
# 	my $score = $csv_entry->{$score_fld_name};
# 	my @id_gffs = select_gff_entries($gff_entries, $id);
# 	my $num_gffs = scalar(@id_gffs);
# 	print "Found $num_gffs gff lines for $id\n";
# 	foreach my $id_gff (@id_gffs)
# 	{
# 	    @{$id_gff}[5] = sprintf("%0.2e",$score);
# 	    print ALL entry_line($id_gff);
# 	    print UP entry_line($id_gff) if ($score >= 0);
# 	    print DOWN entry_line($id_gff) if ($score < 0);
# 	}
#     }


    close(ALL);
#    close(UP);
#    close(DOWN);
}


# - - - - Funcs

sub entry_line
{
    my ($entry) = @_;

    return join("\t",@$entry)."\n";
}

sub select_gff_entries
{
    my ($all_entries, $id) = @_;

    my @select = grep 
    {
	@{$_}[8] =~ /^$id$/i
    } @$all_entries;

    return @select;
}

