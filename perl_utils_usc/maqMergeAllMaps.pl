#!/usr/bin/env perl

use strict;
use File::Find;
use File::Temp qw/tempfile/;
use Cwd;

my $USAGE = "maqMergeAllMaps.pl out_prefix ref.bfa dir1 dir2 ... ";

$::FOLLOW = 0;

die "$USAGE\n" unless (@ARGV >= 3);
my ($out_prefix, $ref_bfa, @dirs) = @ARGV;


my ($tf_fh,$tf) = tempfile("${out_prefix}_XXXX");
$::tmpdir_rel = "./${tf}_d";
`mkdir $::tmpdir_rel`;
$::tmpdir = Cwd::abs_path($::tmpdir_rel);

@::map_files = ();
$::count = 0;


find({ wanted => \&wanted, follow => $::FOLLOW }, @dirs);
#print STDERR join(", ", @::map_files)."\n";
print STDERR "Found $::count files\n";

print $tf_fh " # Generated automatically by maqMergeAllMaps.pl ". join(" ",@ARGV) . "\n";
print $tf_fh "cd \"\$PBS_O_WORKDIR\"\n";
#print $tf_fh "wrap_maq_mapmerge.pl ${out_prefix}.map ". $::tmpdir ."/*\n";
print $tf_fh "maq mapmerge ${out_prefix}.map ". $::tmpdir_rel ."/*\n";
print $tf_fh "wrap_maq_pileup.pl ${out_prefix}.pileup -q 30 -v -P ${ref_bfa} ${out_prefix}.map\n";
close($tf_fh);

my $cmd = "qsub -q laird -l arch=x86_64,walltime=12:00:00 -N ${tf} $tf";
print STDERR $cmd."\n";
`$cmd`;

sub wanted
{
    my $file = $_;
    if (-s $file && ($file=~/sequence\.[1-9].*m\.map$/) && ($file !~ /bad\.map/))
    {
#	push(@::map_files, $File::Find::name) ;
	my $cmd = "ln -s " . Cwd::abs_path($file) . " " . $::tmpdir . "/" . $::count ;
	print STDERR "${cmd}\n";
	`$cmd`;
	$::count++;
    }
}

