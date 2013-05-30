
#  This file generates the xml files in the raw directory from
#  the corresponding files from the Taminations project.
#  Requires $(TAMINATIONS) to be set to the location of that project

OBJDIR = assets
TAMDIRS = b1 b2 ms plus a1 a2 c1 c2 c3a src
TAMTYPES = xml html png
SRC = $(foreach dir,$(TAMDIRS),\
      $(foreach type,$(TAMTYPES),\
      $(wildcard $(TAMINATIONS)/$(dir)/*.$(type))))

SRCNAMES = $(subst $(TAMINATIONS),,$(SRC))
OBJ = $(addprefix $(OBJDIR),$(SRCNAMES))

all : $(OBJ)

debug :
	echo $(SRC)

$(OBJ) : $(SRC)

%.xml :
	copy $(subst /,\,$(subst $(OBJDIR),$(TAMINATIONS),$@)) $(subst /,\,$@)
#  The mobile site requires a viewport tag, but that breaks
#  scrolling for Gingerbread on the app.  So remove the viewport tag here.
%.html :
	perl -p -e "s/.*viewport.*//" $(subst $(OBJDIR),$(TAMINATIONS),$@) >$@
%.png :
	copy $(subst /,\,$(subst $(OBJDIR),$(TAMINATIONS),$@)) $(subst /,\,$@)
