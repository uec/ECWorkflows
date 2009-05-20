#!/usr/bin/perl

use strict;
use Bio::AlignIO;

my $in  = Bio::AlignIO->new(-fh => \*STDIN, '-format' => 'maf');
my $out  = Bio::AlignIO->new(-fh => \*STDOUT, '-format' => 'msf');

while ( my $aln = $in->next_aln() ) 
{
    $out->write_aln($aln);
}
