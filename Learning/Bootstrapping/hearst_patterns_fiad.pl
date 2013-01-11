#!/usr/local/bin/perl -w


use strict;
use Getopt::Long;
use Pod::Usage;

my($i, @stopw, @hearst, $seedsFile, @seeds, $stop, $top, $topic, $instances, $patterns, $tno, $output, $flag, %hEntities);

my $help = 0;

usage() if( ! GetOptions('help|?' => \$help,'t=s' => \$topic, 'p=s' => \$patterns, 'w=s' => \$stop, 'd=s' => \$tno, 'n=i' => \$top, 'o=s' => \$output) );

#usage() if( ! GetOptions('help|?' => \$help,'t=s' => \$topic, 'p=s' => \$patterns, 'w=s' => \$stop, 'n=i' => \$top, 'o=s' => \$seedsFile) );

pod2usage(1) if $help;


sub usage
 {
  print "Usage: filename.pl -t topic -p patterns -n -w stop_word_list -d data_set N\n";
  exit;
 }

open(PAT, $patterns) or die "file $patterns was not found\n";

$i = 0;

while(<PAT>){
	chomp;
	s/\*/$topic/;
	if ($_ !~ /is a/) {s/($topic)/$1s/;}	
	# Initialization: store all good Hearst patterns in an array
 	$hearst[$i++] = $_;
	#if a serach term has a plural form not obtained by adding -s, it
	# should be provided explicitely here like in the following lines
 	#$hearst[0] = "criteria such as";
	#$hearst[1] = "such criteria as";
 	#$hearst[2] = "or other criteria";
	#$hearst[3] = "is a criterion";
	#$hearst[4] = "criteria including";
	#$hearst[5] = "criteria especially";
 	}

close(PAT);

open(STOPW, $stop) or die "file $stop was not found\n";

$i = 0;

while(<STOPW>){
	chomp;
	$stopw[$i++] = $_;	
	}

close(STOPW);

my $tno_data;

open(TNO, $tno) or die "file $tno was not found\n";

while(<TNO>){
	chomp;
	$tno_data = $tno_data.$_;	
	}

close(TNO);


my $out_entities;
	
$out_entities = $tno_data;

# extract all entities by Hearst patterns and rank them
# select top N entities
for(my $k=0; $k<=$#hearst; $k++){
		#$out_entities = `/scratch/emeij/meshEE/meshEE /scratch/emeij/trecgen2006/indri.config.xml -query="#1($hearst[$k])" -printPassages=true`;
		my @hearst_entities;		
		print "$hearst[$k]\n";
	#$out_entities =~ s/(\,|\:|\;|\"|\(|\)|\.)/ $1 /g;
	
	if ($hearst[$k]=~/^($topic)s\s.+/){
		if (@hearst_entities = $out_entities =~ /$hearst[$k] (\S+)/g)
	 	{printf "we found %d entities\n", scalar @hearst_entities; }
	        }
	elsif ($hearst[$k]=~/.+($topic)$/) {
		if (@hearst_entities = $out_entities =~ /(\S+) $hearst[$k]/g)
	 	{printf "we found %d entities\n", scalar @hearst_entities; }
	}
	for(my $j=0; $j<=$#hearst_entities; $j++)
	 {
		# store entities in a hash: (entity, #occ)
		# remove stop list words first
		$flag = 0;
		for(my $m=0; $m <= $#stopw; $m++){
			if (($hearst_entities[$j] eq $stopw[$m]) || (length($hearst_entities[$j])<=2)) {$flag = 1;}}
			if ($flag==0){
			if (!exists($hEntities{$hearst_entities[$j]}))
				{$hEntities{$hearst_entities[$j]} = 1;}
			else {$hEntities{$hearst_entities[$j]}++;}
			#print "$hearst_entities[$j]\n";
		}

	 }
	#print "size of hash \"$hearst[$k]\": " . keys( %hEntities ) . ".\n";
}

my @sorted = reverse sort { $hEntities{$a} cmp $hEntities{$b} } keys %hEntities;

open(SEEDS, ">$output") or die "cannot open $output\n";
if ($#sorted < $top) {$top = $#sorted;}

for(my $j=0; $j<=$top; $j++) {print SEEDS "$sorted[$j]\n";}

close(SEEDS);

__END__

=pod

=head1 NAME

heart_patterns.pl - Extract entities given a set of Hearst patterns

=head1 SYNOPSIS

heart_patterns.pl [options]

Options:

	-h brief help message
	-t topic
	-p file with the Hearst patterns (separated by newline)
	-w file with a stop-word list
	-d your data set (plain txt)
	-n top N extractions to be saved
	-o filename (for top N extractions)

=head1 DESCRIPTION

B<hearst_patterns.pl> takes topic name, a list of Hearst patterns, a number of extractions to be stored and a filename for them.

Please replace any concept (topic) name by * in the Hearst patterns' file. Examples: "is a *", "* such as". This ensures that the pre-defined Hearst patterns can be combined with the topic (specified by B<-t>) and provides a flexibility of concepts (topics) which might be used. 

Given B<./hearst_patterns.pl -t gene -p myHearstPatterns -w MyStopWordList -d MyData -n 20 -o MyGeneNames>, the most frequent 20 Hearst patterns are used with the * replaced by 'gene'('genes'). The extracted gene names are stored in the file 'MyGeneNames'.

=cut