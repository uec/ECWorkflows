#!/usr/bin/perl

use File::Basename;

my ($path) = @ARGV;

my $outfn = "./summaries.htm";

die "Can't write to $outfn\n" unless (open(OUTF, ">$outfn"));

$path =~ s/\/$//g;
my @files = <${path}/*.htm>;
foreach $file (@files) {
    my $base = File::Basename::basename($file);
    print $base . "\t" . $file . "\n";
    print OUTF "<A HREF=\"$file\">$base</A><BR>\n";
}

@files = <${path}/s_*_eland_result.txt>;
push(@files,  <${path}/s_*_sequence.txt>);
push(@files,  <${path}/s_*_tagcount.txt>);
foreach $file (@files) {
    my $cmd = "ln -s $file ./";
    print STDERR "$cmd\n";
    `$cmd`;
}

close(OUTF);
