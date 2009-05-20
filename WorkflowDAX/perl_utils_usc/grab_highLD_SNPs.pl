#!/usr/bin/perl

use strict;

# Constants
my $USAGE = "grab_highLD_SNPs.pl minR2";
my $LD_FILE_TEMPLATE = "/Users/benb/genome_data/hapmap/LD/ld_++++_strongLD.txt";


my ($min_rsq) = @ARGV;


my $seen = {};
RS_LINE: while (my $line = <STDIN>)
{
    chomp $line;
    next if ($line =~ /track/);

    my ($chr, $src, $type, $s, $e, $score, $strand, $frame, $atts) = split(/\t/,$line);
    die "Atts section not formatted correctly: $atts\n" unless ($atts =~ /gene_id \"([^\"]+)\"/);
    my $ref_rs = $1;

    # First echo the reference rs
    print join("\t", $chr, $src, $type, $s, $e, "1.0", $strand, $frame, $atts)."\n";

    # Then the linked ones
    my $cmd = "grep $ref_rs $LD_FILE_TEMPLATE";
    $cmd =~ s/\+\+\+\+/$chr/g;
    print STDERR "${ref_rs}: $cmd\n";
    my $out = `$cmd`;

    my @lines = split(/\n/,$out);
    LD_LINE: foreach my $ld_line (@lines)
    {
	my ($pos1, $pos2, $pop, $rs1, $rs2, $dprime, $rsq, $lod, $fbin) = split(/\s+/,$ld_line);
	next LD_LINE if ($rsq < $min_rsq);
	print STDERR "Found line: $ld_line\n";
	
	my ($other_pos, $other_rs);
	if ($s == $pos1)
	{
	    $other_pos = $pos2;
	    $other_rs = $rs2;
	}
	elsif ($s == $pos2)
	{
	    $other_pos = $pos1;
	    $other_rs = $rs1;
	}
	else
	{
	    die "The following LD entry does not match the position of the reference rs ($ref_rs, $s)\n$ld_line";
	}

	next LD_LINE if ($seen->{$other_rs});
	print gffLine($ref_rs, $other_rs, $chr, $other_pos, $other_pos, $pop, $rsq, $lod);
    }

}

sub gffLine
{
    my ($ref_rs, $other_rs, $chr, $s, $e, $pop, $rsq, $lod) = @_;
    my $out = "";
    
    my $id = sprintf("%s-linkedTo-%s-rsq%0.3f-lod%0.3f-%s",$other_rs, $ref_rs, $rsq, $lod, $pop);

    $out .= join("\t", $chr, "hapmap", "exon", $s, $e, $rsq, "+", ".", "gene_id \"${id}\"; transcript_id \"${id}\";");
    $out .= "\n";

    return $out;
}
