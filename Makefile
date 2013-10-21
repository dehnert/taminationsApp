
#  This file generates the xml files in the raw directory from
#  the corresponding files from the Taminations project.
#  Requires $(TAMINATIONS) to be set to the location of that project
#  On Windows, use Cygwin

#  These are the files to copy from Taminations to the app
TAMDIRS = b1 b2 ms plus a1 a2 c1 c2 c3a src
TAMTYPES = xml html png css dtd
SRC = $(foreach dir,$(TAMDIRS),\
      $(foreach type,$(TAMTYPES),\
      $(wildcard $(TAMINATIONS)/$(dir)/*.$(type))))

#  Generate destinations filename from source filenames
SRCNAMES = $(subst $(TAMINATIONS),,$(SRC))
OBJDIR = assets
OBJ = $(addprefix $(OBJDIR),$(SRCNAMES))
PREVOBJ = $(wildcard $(OBJDIR)/*/*)

#  Dependencies
.PHONY: all clean
all : $(OBJ)

$(OBJ) : $(SRC)

clean :
	-rm $(PREVOBJ)

#  Commands to copy the files
% :
	cp $(subst $(OBJDIR),$(TAMINATIONS),$@) $@
#  The mobile site requires a viewport tag, but that breaks
#  scrolling for Gingerbread on the app.  So remove the viewport tag here.
%.html :
	perl -p -e "s/.*viewport.*//" $(subst $(OBJDIR),$(TAMINATIONS),$@) >$@
